# 📱 Apollo Mobile

<p align="center">
  <strong>Aplicativo Android para técnicos de manutenção fotovoltaica</strong>
</p>

<p align="center">
  <a href="https://github.com/ApolloOficial/Apollo-Mobile/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/ApolloOficial/Apollo-Mobile/actions/workflows/ci.yml/badge.svg"></a>
  <img alt="Android" src="https://img.shields.io/badge/Android-3DDC84">
  <img alt="Java" src="https://img.shields.io/badge/Java-ED8B00">
  <img alt="Retrofit" src="https://img.shields.io/badge/Retrofit-48B983">
  <img alt="CameraX" src="https://img.shields.io/badge/CameraX-4285F4">
</p>

O Apollo Mobile apoia o trabalho dos técnicos na plataforma Apollo, com consulta de placas, leitura de códigos, registro de problemas, ordens de serviço e acesso ao assistente ApolloAI.

## Como funciona

- **Interface:** aplicação Android em Java com View Binding e componentes Material.
- **API:** Retrofit e OkHttp conectam o aplicativo à Apollo API.
- **Leitura de códigos:** CameraX e ML Kit apoiam a identificação de placas.
- **Sincronização:** WorkManager apoia o processamento de tarefas em segundo plano.
- **Eventos:** o módulo de tracking registra eventos de uso; o projeto também declara Firebase Analytics.

```text
Técnico → aplicativo Android → Apollo API → dados operacionais / ApolloAI
```

### Arquitetura do aplicativo

O aplicativo é organizado em um único módulo Android (`app`). As telas são `Activity` agrupadas por funcionalidade em `view/`; cada funcionalidade mantém seus próprios contratos de API e gateways quando aplicável. O fluxo de rede passa pelo cliente compartilhado `ApiClient`, que configura Retrofit, OkHttp e a autenticação da sessão.

```text
MainActivity → StartupRouter → telas em view/
                                  ↓
                 gateway/API da funcionalidade → ApiClient → Apollo API
                                                    ↑
                                      sessão criptografada (session/)
```

O `WorkManager` executa tarefas em segundo plano, incluindo sincronizações. Câmera e leitura de códigos ficam nos fluxos de scanner. Essa organização descreve o código existente sem pressupor camadas ou padrões que o projeto não implementa.

## Início rápido

Requisitos: Android Studio, SDK 36 e emulador ou dispositivo com Android 10 (API 29) ou superior. O CI usa JDK 21; o código do módulo está configurado com compatibilidade Java 11.

```bash
git clone https://github.com/ApolloOficial/Apollo-Mobile.git
cd Apollo-Mobile
```

Abra a pasta no Android Studio e sincronize o Gradle. Confira a configuração Firebase em `app/google-services.json` para o ambiente utilizado.

A URL padrão é `http://10.0.2.2:8080/`, que aponta para a máquina hospedeira no emulador Android. Para outro ambiente, defina `API_BASE_URL` como propriedade Gradle, mantendo a barra final.

```powershell
.\gradlew.bat assembleDebug -PAPI_BASE_URL=https://sua-api/
```

A API deve estar disponível para os fluxos integrados. Execute o aplicativo pelo botão **Run** do Android Studio.

## Funcionalidades

Os módulos incluem autenticação, placas e scanner, ordens de serviço, configurações, sincronização e chatbot. Permissões do Android devem ser concedidas conforme o fluxo utilizado, como acesso à câmera no scanner.

## Testes

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

Os testes instrumentados exigem emulador ou dispositivo conectado. No Linux/macOS, substitua `.\gradlew.bat` por `./gradlew`. Valide manualmente os fluxos alterados com a API.

## Documentação

- [Tracking e experiência de uso](TRACKING_UX.md)
- [Configuração do módulo Android](app/build.gradle.kts)
- [Apollo API](https://github.com/ApolloOficial/Apollo-API-Spring)
- [Assistente ApolloAI](https://github.com/ApolloOficial/Chatbot-ApolloAI)

