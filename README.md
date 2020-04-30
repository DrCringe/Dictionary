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
docker run --name <container_name> --rm -p 8080:8080 -e spring_datasource_url=jdbc:postgresql://<your_ip>:8088/dictionary_db <username>/dictionary-api
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
## Database structure
Dictionary database contains only one schema `entries`
id | word | wordtype | definition
-- | ---- | -------- | ----------
1 | Test | n. | Test definition

All columns are marked with `NOT NULL`, but `wordtype` contains some empty values. Data in SQL script isn't ordered.
## API requests description
Get a page of all entries with a specific nubmer:
#### `GET /entries?page=<page_number>`
Get one entry with a specific id:
#### `GET /entries/<entry_id>`
Get a page with a specific nubmer of entries starting with a specific letter:
#### `GET /enries/letter/<letter>?page=<page_number>`
Get a page with a specific nubmer of entries with a specific word:
#### `GET /entries/word?word=<word>&page=<page_number>`
Get a page with a specific nubmer of entries with a specific word type:
#### `GET /entries/type?type=<word_type>&page=<page_number>`
Get a page with a specific nubmer of entries with a specific word and specific word type:
#### `GET /enries/word+type?word=<word>&type=<word_type>&page=<page_number>`
Get a list of entry suggestions starting with specific letter/word part (live search):
#### `GET /entries/search/<starts_with>`
Add a new entry:
#### `POST /entries`
* Requires a JSON body like:
  ```
  {
    "word": "<word>",
    "wordtype": "<word_type>.",
    "definition": "<definition>"
  }
  ```
Modify an existing entry (also requires JSON body):
#### `PUT /entries/<entry_id>`
Modify definition of an entry with a specific id:
#### `PATCH /entries/<entry_id>?newDefinition=<new_definition>`
Delete an entry:
#### `DELETE /entries/<entry_id>`
If dictionary API throws an exception related to invalid request, it responds with a specific http status code and exception 
message inside `Message` header, response body should be empty except one case - when requested word had a typo and API was 
able to find some alternatives using fuzzy search algorithm. In that case a body would contain these alternatives.
