# Voorraadbeheer

Android-app om je voorraad te beheren over meerdere locaties (bv. kast, koelkast, camping). Scan een product met de camera, de app zoekt automatisch naam/merk/foto op en je voegt het toe aan een locatie. Overzichten, een boodschappenlijst en meldingen bij lage voorraad of naderende houdbaarheidsdatum zitten er ook in.

## Features

- **Barcode scannen** (CameraX + ML Kit, alles on-device) — productinfo wordt automatisch opgehaald via [Open Food Facts](https://world.openfoodfacts.org/) (gratis, geen API-key)
- **Meerdere locaties** (kast, koelkast, camping, zelf uit te breiden)
- **Dashboard** met totalen, lage voorraad en bijna-verlopen producten per locatie
- **Houdbaarheidsdatum (THT)** bijhouden met melding vooraf
- **Boodschappenlijst**, automatisch gevuld met producten onder de ingestelde minimumvoorraad, plus handmatige items
- **Handmatig product toevoegen** (voor producten zonder barcode)
- **Homescreen widget** met aantal lage-voorraad / bijna-verlopen items
- **Cloud-sync via Firebase** (Firestore + Google Sign-In), werkt ook offline dankzij Firestore's lokale cache
- **Samen voorraad bijhouden** met huisgenoten via een deelcode (bv. jij en je partner zien dezelfde voorraad)

## Techniek

Kotlin, Jetpack Compose (Material 3), Hilt, Firebase (Auth + Firestore), CameraX + ML Kit Barcode Scanning, Retrofit (Open Food Facts), Coil, WorkManager, Glance (widget).

## Kosten

Dit project is volledig gratis te gebruiken:
- Android Studio: gratis
- Firebase gratis "Spark"-laag: geen creditcard nodig, ruim voldoende voor persoonlijk gebruik
- Open Food Facts API: gratis, geen key
- APK op je eigen telefoon installeren: gratis, Play Store is niet nodig

## Setup

### 1. Android Studio

Download en installeer [Android Studio](https://developer.android.com/studio) (gratis). Open dit project (map met `build.gradle.kts`) — Android Studio pakt de rest (SDK-componenten, Gradle-sync) automatisch op.

### 2. Firebase-project aanmaken

1. Ga naar [console.firebase.google.com](https://console.firebase.google.com) en maak een nieuw (gratis) project aan.
2. Voeg een Android-app toe aan het project met pakketnaam **`nl.voorraadbeheer.app`**.
3. Download het gegenereerde `google-services.json` bestand.
4. Plaats dit bestand in `app/google-services.json` (dezelfde map als `app/build.gradle.kts`). Dit bestand staat in `.gitignore` en wordt dus nooit gecommit — er staat alleen een `app/google-services.json.template` in de repo ter referentie.

### 3. Authenticatie inschakelen

In de Firebase Console: **Authentication → Sign-in method → Google** inschakelen.

### 4. Firestore inschakelen

In de Firebase Console: **Firestore Database → Database aanmaken** (kies "production mode" en een regio in de buurt, bv. `europe-west`).

Kopieer daarna de inhoud van [`firestore.rules`](firestore.rules) in dit project naar **Firestore Database → Rules** in de Firebase Console en publiceer de regels. Deze regels zorgen dat alleen leden van hetzelfde huishouden bij die voorraad kunnen.

### 5. Storage inschakelen (voor foto's bij handmatig toegevoegde producten)

In de Firebase Console: **Storage → Aan de slag** (kies dezelfde regio als je Firestore-database).

Kopieer daarna de inhoud van [`storage.rules`](storage.rules) naar **Storage → Rules** en publiceer.

### 6. Bouwen en installeren

In Android Studio: **Run ▶** met je telefoon aangesloten (USB-debugging aan) of een emulator. Dit bouwt een debug-APK en installeert 'm direct — geen Play Store nodig.

## Samen met huisgenoten

Bij de eerste keer inloggen krijg je automatisch je eigen "huishouden". Ga naar **Instellingen** om de deelcode te zien en te delen (bv. met je partner). Als iemand anders diezelfde code invoert bij **Instellingen → Lid worden van een huishouden**, zien jullie vanaf dat moment dezelfde voorraad, locaties en boodschappenlijst. Na het aansluiten bij een huishouden moet de app opnieuw opgestart worden zodat de gedeelde data geladen wordt.

## Belangrijke beperking van deze sessie

Dit project is gebouwd in een cloud-omgeving zonder Android SDK en zonder toegang tot Google's Maven-repository (waar Android Gradle Plugin, Jetpack- en Firebase-libraries vandaan komen — die zijn hier netwerkmatig geblokkeerd). Daardoor kon de app **niet** in deze sessie gecompileerd of getest worden; de code is zorgvuldig met de hand nagelopen op consistentie (imports, resource-verwijzingen, navigatie), maar een eerste build in Android Studio kan alsnog kleine compilatiefouten aan het licht brengen. Doe na het openen van het project in Android Studio een **Gradle Sync** en los eventuele meldingen op — geef gerust een seintje als je daarbij vastloopt.

## Projectstructuur

```
app/src/main/java/nl/voorraadbeheer/app/
├── data/           # Modellen, Firestore-repositories, Open Food Facts API
├── di/             # Hilt-modules (Firebase, Retrofit)
├── ui/             # Een package per scherm (dashboard, inventory, scan, addproduct, ...)
├── notifications/  # WorkManager-check voor lage voorraad / THT + notificaties
└── widget/         # Homescreen widget (Glance)
```

## Ideeën voor later

- Verbruiksstatistieken (per week/maand, meest gebruikte producten)
- Receptensuggesties op basis van wat in voorraad is
- Export naar CSV/Excel
- Batch-scannen (meerdere producten achter elkaar zonder tussenscherm)
