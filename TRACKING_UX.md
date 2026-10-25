# Tracking UX — alterações e teste

## Causa confirmada
O aparelho inspecionado tinha um APK atualizado em 17/10/2026, anterior ao tracking (23/10).
Não havia apollo_tracking.db nem banco do WorkManager no sandbox desse APK.
Não basta reiniciar o aplicativo: é necessário instalar/executar o build novo pelo Android Studio.

## Ajustes
- Spring lê .env na raiz do projeto via spring.config.import; variáveis do ambiente continuam com precedência.
- Novos eventos: navegação por Activity, tempo por tela, foreground/background, cliques com ID fixo,
  cliques repetidos, filtros de OS/histórico, busca de placas (somente tamanho e contagem),
  estados vazios, tentativas de repetição, feedback visual, permissão de câmera, duração e falhas de API.
- Funis de laudo e relato de problema: início, envio, validação, sucesso e saída sem concluir.
- Eventos de negócio existentes continuam presentes. Há 25 eventos permitidos no cliente e 3 exclusivos do servidor.
- Não coletamos textos de formulários, busca, chat, senhas, tokens, URLs completas ou corpos de respostas.
- Cliques repetidos são um sinal para análise, não prova automática de problema de UX.
- Um processo morto pelo Android não garante evento de saída. Inferir abandono por sessão sem conclusão;
  form_abandoned registra somente o fechamento observado de um formulário iniciado.

## Antes de testar
1. No Compass, abra apollo_analytics > tracking_events > Validation.
2. Cole o conteúdo completo de tracking-validator.json da raiz do Spring. Mantenha Strict e Error.
   Não apague a collection nem seus índices.
3. No .env da raiz do Spring, configure TRACKING_ENABLED=true e MONGODB_URI (ou TRACKING_MONGO_URI).
   Use credenciais novas: as anteriores foram expostas no chat. Não coloque URI no Android.
4. Reinicie o Spring com diretório de trabalho na raiz do projeto.
5. Instale o APK novo preservando os dados do app, entre com usuário autenticado e navegue pela Home e OS.
6. No Logcat filtre ApolloTracking: Queued event=... indica coleta, Sync acknowledged count=... confirma aceite.
7. Atualize Documents no Compass. O envio é assíncrono e depende de rede e autenticação válidas.

## Diagnóstico
O Android agora informa HTTP e código seguro da falha, sem imprimir o corpo da resposta.
O Spring diferencia TRACKING_DISABLED, MONGO_AUTHENTICATION_FAILED, MONGO_PERMISSION_DENIED,
MONGO_SCHEMA_REJECTED, MONGO_TLS_FAILED, MONGO_CONNECTION_TIMEOUT e MONGO_DNS_FAILED.
Falhas transitórias não apagam eventos pendentes; rejeições permanentes continuam em quarentena local.

## Limites da validação neste ambiente
Os testes unitários do Spring são executáveis. A compilação Android e a verificação Atlas encontraram
uma falha de socket interno do Java: Unable to establish loopback connection / Invalid argument: connect.
Isso impediu gerar/instalar um APK novo e confirmar gravação ponta a ponta aqui.
Não é evidência de senha incorreta ou indisponibilidade do seu cluster.
O teste opcional TrackingAtlasDiagnosticsTest é somente leitura e exige TRACKING_DIAGNOSTICS=true
e MONGODB_URI ou TRACKING_MONGO_URI no ambiente do processo de teste. Não grava nem exclui documentos.
