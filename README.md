# Dictionary
Dictionary project consists of three parts:
* dictionary-db - PostgreSQL database that contains [OPTED dictionary](http://www.mso.anu.edu.au/~ralph/OPTED/)
* dictionary-api - Spring Boot Rest API
* dictionary-client-react - React JS web-client for that API
## Installing
Firstly, you will need PostgreSQL server with dictionary database. By default, dictionary-api looking for server 
on `http://localhost:8088`, you can change that by overriding `spring.datasource.url` property.
To build and run docker PostgreSQL container with dictionary database, run
```
docker build -t <username>/dictionary-postgres .
docker run --name <container_name> --rm -e POSTGRES_PASSWORD=postgres -p 8088:5432 -d <username>/dictionary-postgres
```
inside dictionary-db directory.
After that, you can run dictionary API itself. In order to do that, run
```
gradlew bootRun
```
inside dictionary-api directory. Alternatively, you can build the project and run the `.jar` executable. If you prefer to
dockerize dictionary-api, run
```
docker build -t <username>/dictionary-api .
docker run --name <container_name> --rm -e spring_datasource_url=jdbc:postgresql://<your_ip>:8088/dictionary_db <username>/dictionary-api
```
Lastly, you need to start client app. You can either run `npm start` in dictionary-client-react directory if you have
node.js and npm installed locally on your system, or also dockerize it
```
docker build -t <username>/dictionary-client-react .
docker run --name dict_client --rm -it -p 4040:3000 <username>/dictionary-client-react
```
### Testing
Dictionary API tests also require docker images of dictionary database and dictionary API. By default, image names set to
`dictionary-postgres` and `dictionary-api` respectively.
