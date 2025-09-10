# Synchronisation Set Move Route - Commandes 509

## Problématique RPG Maker XP

Dans RPG Maker XP, une commande "Set Move Route" (code 209) fonctionne différemment des autres commandes. Elle contient une structure JSON avec une propriété `list` qui stocke les commandes de mouvement internes. Chaque commande de mouvement non-terminatrice (code != 0) dans cette liste doit avoir une commande 509 correspondante qui la suit immédiatement dans la liste principale des commandes de l'événement.

## Architecture de données

### Structure d'une commande 209
```json
{
  "code": 209,
  "parameters": [
    0,  // Target ID (-1 = Player, 0 = This Event, >0 = Event ID)
    {
      "repeat": false,
      "skippable": false, 
      "list": [
        {"code": 1, "parameters": []},        // Move Down
        {"code": 45, "parameters": ["script"]}, // Script command
        {"code": 0, "parameters": []}         // Terminator (ignored)
      ]
    }
  ]
}
```

### Commandes 509 correspondantes
Chaque commande non-terminatrice génère une commande 509 :
```java
EventCommand cmd509_1 = new EventCommand(509, "0", [{"code": 1, "parameters": []}]);
EventCommand cmd509_2 = new EventCommand(509, "0", [{"code": 45, "parameters": ["script"]}]);
```

## Mécanisme de synchronisation

### 1. Vérification (verifySetMoveRouteSync)

La méthode parcourt la `list` interne de la commande 209 :
```java
int expectedCount = 0;
for (int i = 0; i < moveList.length(); i++) {
    JSONObject moveCmd = moveList.getJSONObject(i);
    if (moveCmd.getInt("code") != 0) {  // Ignore terminators
        expectedCount++;
    }
}
```

Puis compte les commandes 509 consécutives qui suivent :
```java
int actualCount = 0;
for (int i = setMoveRouteIndex + 1; i < commands.size(); i++) {
    if (commands.get(i).getCode() == 509) {
        actualCount++;
    } else {
        break;  // Must be consecutive
    }
}
```

### 2. Réparation automatique (repairSetMoveRouteSync)

Si `expectedCount != actualCount`, la réparation supprime puis reconstruit :

**Suppression des anciennes 509 :**
```java
int i = setMoveRouteIndex + 1;
while (i < commands.size() && commands.get(i).getCode() == 509) {
    commands.remove(i);
    commandListModel.remove(i);
}
```

**Reconstruction complète :**
```java
int insertIndex = setMoveRouteIndex + 1;
for (int j = 0; j < moveList.length(); j++) {
    JSONObject moveCmd = moveList.getJSONObject(j);
    if (moveCmd.getInt("code") != 0) {
        EventCommand cmd509 = new EventCommand(509, "0", new JSONArray().put(moveCmd));
        commands.add(insertIndex, cmd509);
        commandListModel.add(insertIndex, cmd509);
        insertIndex++;
    }
}
```

### 3. Sauvegarde avec synchronisation (updateCommand509List)

Lors de la fermeture de l'éditeur, le processus est identique à la réparation :

1. **Suppression totale** des 509 existantes
2. **Reconstruction complète** basée sur `modifiedMoveRouteList`

La variable `modifiedMoveRouteList` contient uniquement les commandes non-terminatrices, filtrées lors du chargement initial.

## Points clés

### Pas de mise à jour partielle
Il n'y a pas de logique pour "modifier" ou "insérer" des 509 spécifiques. Le système fait toujours :
- DELETE ALL 509s
- REBUILD ALL from list

### Ordre strict
Les commandes 509 doivent être **consécutives** et **immédiatement** après la 209. Aucune autre commande ne peut s'intercaler.

### Filtrage des terminateurs
La commande `{"code": 0}` termine la liste interne mais ne génère jamais de commande 509.

### Variables critiques
- `setMoveRouteIndex` : Position de la commande 209 dans la liste principale
- `modifiedMoveRouteList` : Liste filtrée des commandes de mouvement (sans terminateurs)
- `fullCommandList` : Référence vers la liste complète des commandes de l'événement

Cette approche "delete-and-rebuild" garantit une synchronisation parfaite mais nécessite de passer la liste complète des commandes à l'éditeur, contrairement aux autres éditeurs de commandes qui travaillent sur des copies isolées.