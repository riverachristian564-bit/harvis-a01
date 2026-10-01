# HARVIS A01

Cliente Android nativo y liviano para Samsung Galaxy A01 / Android 10.

La app se conecta al Shared Core de HARVIS MK1 en la PC mediante `/health`, `/v1/bootstrap` y `/v1/chat`.

## Uso
1. HARVIS MK1 debe estar abierto en la PC y escuchando en el puerto 8765.
2. En la app, configurar `http://IP_LOCAL_PC:8765`.
3. Si HARVIS usa token de bridge, ingresarlo; de lo contrario dejar vacío.
4. Pulsar CONECTAR y enviar un mensaje.

El APK debug se genera automáticamente con GitHub Actions.