# ms-bff-hotel

Backend for Frontend (puerto 8080) de AndesStay / Hotel Boutique (DSY1107, EP2).
Recibe las llamadas del frontend a través de API Gateway, valida de nuevo el JWT de Cognito, aplica la autorización por rol y reenvía a los microservicios. No se conecta a la base de datos.

## Rutas y roles
| Ruta | Acceso |
|---|---|
| `/habitaciones/disponibles` (GET) | Público |
| `/reservas`, `/usuarios` | Usuario autenticado (CLIENTE o ADMIN) |
| `POST /habitaciones`, `GET /reservas`, `PUT /reservas/*/checkin\|checkout\|cancelar` | Solo ADMIN |
| `/api/admin/**` (administración de RabbitMQ, proxy a `ms-rabbit-admin`) | Solo ADMIN (403 para otros roles) |

Sin token o con token inválido responde `401`.

## RabbitMQ
El BFF solo publica (`SolicitudEventPublisher`) y administra la topología a través de `ms-rabbit-admin`; los consumidores viven en reservas y auth.

## Variables de entorno (sin valores)
Se leen desde un `.env` (no versionado).

| Variable | Uso |
|---|---|
| `AWS_REGION`, `COGNITO_USER_POOL_ID` | Validación del JWT |
| `RESERVAS_SERVICE_URL`, `USUARIOS_SERVICE_URL` | URLs de ms-reservas (8082) y ms-auth (8081) |
| `RABBIT_ADMIN_SERVICE_URL` | URL de ms-rabbit-admin (puerto 8090 en la EC2 del clúster) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (URL del frontend) |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_VHOST`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | Broker |

## Ejecutar y probar
```bash
mvn clean test
mvn spring-boot:run
```
