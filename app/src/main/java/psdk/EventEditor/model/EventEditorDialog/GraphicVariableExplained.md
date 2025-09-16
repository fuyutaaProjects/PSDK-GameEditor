# Manuel RPG Maker XP - Paramètres des Sprites

## Structure des Spritesheets RPG Maker XP

### Format des fichiers
- **Taille :** 128x128 pixels (grille 4x4)
- **Frame individuelle :** 32x32 pixels
- **Format :** PNG
- **Emplacement :** `Graphics/Characters/`

### Organisation de la grille 4x4

```
     Col 1   Col 2   Col 3   Col 4
     (0)     (1)     (2)     (3)
  ┌───────┬───────┬───────┬───────┐
L1│ Down  │ Down  │ Down  │ Down  │ Direction 2
  │Frame 0│Frame 1│Frame 2│Frame 3│ 
  ├───────┼───────┼───────┼───────┤
L2│ Left  │ Left  │ Left  │ Left  │ Direction 4
  │Frame 0│Frame 1│Frame 2│Frame 3│
  ├───────┼───────┼───────┼───────┤
L3│Right  │Right  │Right  │Right  │ Direction 6
  │Frame 0│Frame 1│Frame 2│Frame 3│
  ├───────┼───────┼───────┼───────┤
L4│  Up   │  Up   │  Up   │  Up   │ Direction 8
  │Frame 0│Frame 1│Frame 2│Frame 3│
  └───────┴───────┴───────┴───────┘
```

## Paramètres des Events

### 1. Character Index
- **Valeur :** Entier (généralement 0-7)
- **Fonction :** Identifie QUEL personnage/spritesheet utiliser
- **Important :** N'affecte PAS la position dans la spritesheet elle-même
- **Usage :** RPG Maker XP utilise cet index pour différencier plusieurs personnages dans certains contextes, mais chaque fichier .png contient une grille complète 4x4

### 2. Direction
- **Valeurs possibles :**
  - `2` = Down (Bas) → Ligne 1
  - `4` = Left (Gauche) → Ligne 2  
  - `6` = Right (Droite) → Ligne 3
  - `8` = Up (Haut) → Ligne 4
- **Fonction :** Détermine quelle LIGNE de la grille utiliser
- **Note :** Les valeurs 2,4,6,8 correspondent au pavé numérique

### 3. Pattern
- **Valeurs possibles :** 0, 1, 2, 3
- **Fonction :** Détermine quelle COLONNE de la grille utiliser
- **Usage :** 
  - Pattern 0 = Frame d'idle/repos
  - Patterns 1,2,3 = Frames d'animation de marche
  - Pattern 0 et 2 sont souvent identiques (poses neutres)
  - Pattern 1 et 3 montrent les jambes en mouvement

## Calcul des Coordonnées

### Formule
```java
frameX = pattern * 32
frameY = directionIndex * 32

// Où directionIndex est :
// Direction 2 (Down) → directionIndex 0
// Direction 4 (Left) → directionIndex 1  
// Direction 6 (Right) → directionIndex 2
// Direction 8 (Up) → directionIndex 3
```

### Exemples concrets

| Character | Index | Direction | Pattern | Position RMXP | Coordonnées |
|-----------|-------|-----------|---------|---------------|-------------|
| aborigene_m_1_walk | 0 | 2 (Down) | 0 | Col 1, Ligne 1 | (0, 0) |
| amanda_walk | 0 | 4 (Left) | 0 | Col 1, Ligne 2 | (0, 32) |
| astolfo_walk | 3 | 2 (Down) | 3 | Col 4, Ligne 1 | (96, 0) |
| apollo_walk | 2 | 6 (Right) | 2 | Col 3, Ligne 3 | (64, 64) |
| castex_2_walk | 3 | 8 (Up) | 3 | Col 4, Ligne 4 | (96, 96) |
| alvis_walk | 0 | 8 (Up) | 0 | Col 1, Ligne 4 | (0, 96) |

## Points importants

### Character Index
- **Ne modifie PAS** la position dans la spritesheet
- Utilisé par RPG Maker XP pour des logiques internes
- Chaque fichier .png est indépendant et contient sa propre grille 4x4

### Animation
- Les 4 patterns d'une même direction créent une animation de marche
- Cycle typique : 0 → 1 → 2 → 3 → (répète)
- Pattern 0 et 2 = poses neutres (pied au centre)
- Pattern 1 et 3 = poses de mouvement (pieds écartés)

### Directions dans le code
Les directions utilisent les valeurs du pavé numérique :
```
  8
  ↑
4 ← → 6
  ↓
  2
```

Cette convention permet une logique intuitive pour le mouvement dans un jeu 2D vu du dessus.