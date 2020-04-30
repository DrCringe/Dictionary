import React from 'react'

const About = () => {
  return (
    <div className="container">
      <div className="bg-white rounded shadow-sm mx-2 mt-2 mb-3 py-3 px-4">
        <h1 className="font-weight-light pb-2"><b>About</b> Dictionary Client</h1>
        <hr/>
        <p>A client web application for Dictionary Spring Boot Rest API built with React JS framework. </p>
        <h2 className="font-weight-light mt-2">Requirements</h2>
        <hr/>
        <p>
          This app requires dictionary API running for correct operation, which in turn requires dictionary database. By default, it's looking for an api server 
          on default Spring Boot location - <code>http://localhost:8080</code><br/>If you changed port or url of API server, you need to specify it in project environment 
          variable <code>REACT_APP_DICTIONARY_API_URL</code> in file <code>.env</code> in project root directory.
        </p>
        <p>
          It also requires CORS enabled for <code>GET</code>, <code>POST</code>, <code>PUT</code>, <code>PATCH</code> and <code>DELETE</code> methods and with exposed 
          header <code>Message</code> that is used to transfer API exception messages to client. If you did not override default API COPS settings, you are good to go.
        </p>
        <p>
          You can find all source code on <a href="https://github.com/DrCringe/Dictionary">project github repository</a>, including database creation SQL script.
        </p>
        <h2 className="font-weight-light mt-2">Resources</h2>
        <hr/>
        <p>
          This project uses <a href="http://www.mso.anu.edu.au/~ralph/OPTED/">The Online Plain Text English Dictionary</a>
        </p>
        <div className="row my-4">
          <div className="col">
            <a href="https://spring.io/"><img src={ require('../logo/spring-logo.svg') } alt="" height="75" /></a>
          </div>
          <div className="col pl-4">
            <a href="https://www.oracle.com/java/technologies/persistence-jsp.html"><img src={ require('../logo/jpa-logo.svg') } alt="" height="75" /></a>
          </div>
          <div className="col">
            <a href="https://hibernate.org/"><img src={ require('../logo/hibernate-logo.svg') } alt="" height="75" /></a>
          </div>
          <div className="col">
            <a href="https://reactjs.org/"><img src={ require('../logo/logo-react-blue-1.svg') } alt="" height="75" /></a>
          </div>
        </div>
      </div>
    </div>
  );
}

export default About;