# Deployment

## Backend on Render

Create a PostgreSQL database and a Web Service using this repository. Set the Web Service root directory to `shiptrack-pro`.

Use these commands if configuring the service manually:

```text
Build: ./mvnw clean package -DskipTests
Start: java -jar target/shiptrack-pro-0.0.1-SNAPSHOT.jar
```

Set these Render environment variables:

```text
DB_URL=jdbc:postgresql://<internal-host>:5432/<database>
DB_USERNAME=<database-user>
DB_PASSWORD=<database-password>
JWT_SECRET=<long-random-secret>
CORS_ALLOWED_ORIGINS=https://<your-vercel-app>.vercel.app
GOOGLE_MAPS_API_KEY=<optional-google-key>
MAIL_USERNAME=<optional-mail-username>
MAIL_PASSWORD=<optional-mail-password>
```

The backend uses local defaults only when these variables are absent. Never use those defaults in production.

## Frontend on Vercel

Import the repository, set the project root to `frontend`, and use the detected Next.js settings. Add:

```text
NEXT_PUBLIC_API_URL=https://<your-render-service>.onrender.com
```

After the Vercel URL is known, set `CORS_ALLOWED_ORIGINS` on Render to that exact URL, redeploy the backend, and then redeploy the frontend.

## Local run

```powershell
cd frontend
npm run dev

cd ..\shiptrack-pro
.\mvnw.cmd spring-boot:run
```