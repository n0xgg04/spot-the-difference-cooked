# Admin Uploader

A small JavaFX tool to create image sets for the Spot The Difference game.

Features:

- Load a left and a right image (PNG/JPG)
- Click on the left image area to add difference points (radius configurable)
- Preview circles mirrored on both images
- Save to MySQL database (tables `image_sets`, `image_differences`)

## Configure

Edit `admin/src/main/resources/admin-config.properties` to point to your MySQL instance.

## Run

In the repository root:

```powershell
mvn -pl admin javafx:run
```

## Database schema

If you haven't applied the schema additions yet, run:

```powershell
mysql -u root -p < db/schema.sql
```
