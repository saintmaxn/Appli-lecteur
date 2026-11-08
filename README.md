# StreamPulse IPTV (Android)

Kotlin + Jetpack Compose + Media3 (ExoPlayer, HLS/DASH/TS). Moteur de recommandation par genre
(fréquence × récence), historique persistant, import M3U (URL, fichier, API Xtream via get.php).

## Lancer
1. Android Studio (Koala+) → File > Open → dossier `StreamPulseIPTV`
2. Laisser la sync Gradle se terminer (JDK 17)
3. Run ▶ sur un émulateur ou un téléphone

## Structure
- `data/` : modèles, parseur M3U, stockage (historique + playlist)
- `recommendation/RecommendationEngine.kt` : l'algorithme
- `ui/` : ViewModel (ExoPlayer), écrans Compose, thème
