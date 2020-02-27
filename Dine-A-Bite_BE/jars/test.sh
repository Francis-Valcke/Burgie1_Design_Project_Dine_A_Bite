../gradle bootJar
docker build -t dineabite .
read -p "Press enter to continue"
docker run -p 8080:8080 dineabite
