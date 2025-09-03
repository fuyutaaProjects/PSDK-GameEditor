## 📚 Comprendre les IDs de tuiles (`data`) dans RPG Maker XP

RPG Maker XP encode chaque tuile d’une carte sous forme d’un identifiant numérique (`tile ID`). Ces IDs sont utilisés dans les blocs `data:` pour représenter la disposition des tuiles sur la carte, ligne par ligne.

---

### 🧠 Structure des IDs

Les IDs de tuiles sont divisés en deux groupes principaux :

| ID         | Description                                 |
|------------|---------------------------------------------|
| `0 – 383`  | **Autotiles** (tuiles automatiques spéciales) |
| `384+`     | **Tuiles normales** du tileset principal      |

---

### 🧮 Comment sont calculés les IDs des tuiles normales ?

Les tuiles normales (celles qu’on voit dans la grille du bas du tileset) sont numérotées **à partir de 384**.

Les IDs sont calculés **ligne par ligne**, de gauche à droite, en commençant par la première tuile tout en haut à gauche de la grille principale.

#### ✅ Exemple

Si la tuile que tu places est la **6e case** de la **2e ligne** du tileset principal :

- Chaque ligne a généralement **8 colonnes**
- L’index de base est : `384`
- Calcul de l’ID :

```txt
ID = 384 + (ligne * nb_colonnes) + colonne
ID = 384 + (1 * 8) + 5 = 397
