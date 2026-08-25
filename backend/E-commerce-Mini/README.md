# E-commerce Mini - Google Login

This project now supports Google OAuth2 login in addition to the existing username/password JWT flow.

## Google login flow

1. Open Spring Security Google authorization endpoint:

```text
GET /oauth2/authorization/google
```

2. After Google login succeeds, the backend will:
   - create a local `User` if the email does not exist yet,
   - assign the default `USER` role,
   - generate the application JWT,
   - redirect to the frontend redirect URL with the token in the URL fragment.

3. Configure the frontend to read the token from:

```text
http://localhost:5173/oauth2/redirect#token=<JWT>
```

## Configuration

Update `src/main/resources/application.yaml` if needed:

- `spring.security.oauth2.client.registration.google.client-id`
- `spring.security.oauth2.client.registration.google.client-secret`
- `app.oauth2.redirect-uri`
- `jwt.signerKey`

## Existing login

The normal login endpoint still works:

```text
POST /api/auth/login
```

with `{ "username": "...", "password": "..." }`.

