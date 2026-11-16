Spin Poker 10
=============

Jeu Android de type Spin Poker (machine à sous + poker), joué en vertical.

- 5 rouleaux de cartes, grille 5 x 3, 10 lignes de paiement.
- Chaque ligne forme une main de poker de 5 cartes, évaluée en Jacks or Better.
- Table des gains classique : Quinte Flush Royale 250x, ... Paire de Valets 1x.
- Deux phases comme sur un vrai Spin Poker : DISTRIBUER, puis GARDER (hold) des
  rouleaux et TIRAGE. Les rouleaux gardés ne sont pas relancés.
- Crédits virtuels gratuits, divertissement uniquement. Aucun argent réel,
  conforme aux politiques Google Play (jeu d'argent interdit).
- Sons synthétiques entraînants (distribution, arrêt de rouleau, gain, fanfare).

Stack
-----

- Kotlin, Jetpack Compose, Material 3, MVVM + StateFlow.
- minSdk 24, target/compileSdk 34. Portrait verrouillé.
- Sons générés par script Python (aucun asset binaire dans le dépôt).
- Tests unitaires JUnit sur l'évaluateur de mains de poker.

Build local
-----------

Prérequis : JDK 17, Android SDK 34, Gradle 8.9+.

    python3 tools/generate_sounds.py   # crée app/src/main/res/raw/*.wav
    gradle assembleDebug               # APK debug
    gradle test                        # tests unitaires

APK debug : app/build/outputs/apk/debug/app-debug.apk

Build release signé :

    keytool -genkeypair -v -keystore release.keystore -alias spinpoker -keyalg RSA -keysize 2048 -validity 10000
    gradle assembleRelease \
      -Pandroid.injected.signing.store.file=release.keystore \
      -Pandroid.injected.signing.store.password=**** \
      -Pandroid.injected.signing.key.alias=spinpoker \
      -Pandroid.injected.signing.key.password=****

CI
--

Le workflow GitHub Actions (.github/workflows/android.yml) génère les sons,
lance les tests et produit l'APK en artefact téléchargeable à chaque push.

Structure
---------

    app/src/main/java/com/sarajane/spinpoker10/
      MainActivity.kt                 Point d'entrée
      game/GameEngine.kt             Tirage des rouleaux (paquets 52 cartes)
      game/GameViewModel.kt          Machine à états : IDLE->SPINNING->HOLD->DRAWING->RESULT
      model/Card.kt, HandEvaluator.kt, Paylines.kt
      sound/SoundManager.kt          SoundPool, 8 sons
      ui/GameScreen.kt               Grille, cartes, contrôles, table des gains
      ui/theme/                      Thème casino sombre
    tools/generate_sounds.py         Générateur de WAV (stdlib Python uniquement)
    app/src/test/                    Tests JUnit de l'évaluateur de mains

Note de conformité
------------------

Jeu de divertissement à crédits virtuels, sans achat intégré ni mise en argent
réel. Publication Play Store possible sous cette forme uniquement.
