# FLASH80
- Flash80 acts as a proxy between user and server hosting the site while at the same time caching responses to drastically improve speed
## Requirements
- docker/docker-compose
## Configuration
- Managed by the file ./flash80/src/main/resources/application.yml
- **hosts** property manages the backend hosts. Each host can have many sites. The current file contains an example change it accordingly to point to your sites.
- **includes/excludes** determine what flash80 should cache or not. The current example is tweaked for wordpress sites.
## Build
- **docker-compose -f docker-compose.build.yml up**
    - First build will take around 1 minute due to downloading dependencies
    - Configuration change requires a rebuild, will be fixed later
## Run
- Extract data/geo-databases.zip to data/
- **docker-compose up**
## Manage
- Once container is up use **https://127.0.0.1/flash80/cache** to manage the cache