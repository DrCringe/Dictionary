import React, { Component, useState, useEffect } from 'react';
import { Switch, Route, Link, withRouter, Redirect } from 'react-router-dom';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faEdit, faTrashAlt } from '@fortawesome/free-solid-svg-icons';
import { usePopper } from 'react-popper';
import LinkButton from './components/LinkButton';
import Dashboard from './components/Dashboard';
import About from './components/About';

import './App.css';

library.add(faEdit, faTrashAlt);

class App extends Component {
  

  constructor(props) {

    super(props);

    this.state = {
      word: ''
    };

    this.handleWordChange = this.handleWordChange.bind(this);
  }

  handleWordChange(event) {
    this.setState({word: event.target.value});
  }

  render() {
    return (
      <div className="bg-light">
        <nav className="navbar navbar-expand-lg navbar-light bg-white">
          <div className="container-md">
            <a className="navbar-brand mb-n1" href="/entries">
              <img src={ require('../src/logo/logo.svg') } width="50" height="50" className="d-inline-block align-center" alt="" />
              Dictionary Client
            </a>
            <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
              <span className="navbar-toggler-icon"></span>
            </button>
          
            <div className="collapse navbar-collapse ml-5" id="navbarSupportedContent">
              <ul className="navbar-nav mr-auto">
                <li className="nav-item active">
                  <Link className="nav-link" to="/entries">Home</Link>
                </li>
                <li className="nav-item ml-2">
                  <Link className="nav-link" to="/entries/add">Add</Link>
                </li>
                <li className="nav-item ml-2">
                  <Link className="nav-link" to="/about">About</Link>
                </li>
              </ul>
            </div>
          </div>
        </nav>

        <SearchNav history={this.props.history} />
      
        <Switch>
          <Route exact path="/">
            <Redirect to="/entries" />
          </Route>
          <Route path="/entries" component={Dashboard} />
          <Route path="/about" component={About} />
        </Switch>

        <footer className="bg-white text-muted border-top">
          <div className="container">
            <div className="media mx-2 my-4">
              <span className="media-left">
                <img src={ require('../src/logo/polytech-logo_small_en.svg') } alt="" height="50" />
              </span>
              <div className="media-body ml-5">
                <p>
                  Peter The Great St.Petersburg Polytechnic University • Institute of Computer Science And Technology <br/>
                  High School of Software Engineering • 2020
                </p>
              </div>
            </div>
          </div>
        </footer>
      </div>
    );
  }

}

const SearchNav = ({ history }) => {

  const [referenceElement, setReferenceElement] = useState(null);
  const [popperElement, setPopperElement] = useState(null);
  const [word, setWord] = useState('');
  const [suggestions, setSuggestions] = useState(null);
  const [isOpen, setOpen] = useState(false);
  const [isFocused, setFocused] = useState(false);
  const {styles, attributes} = usePopper(referenceElement, popperElement);

  useEffect(() => {

    if (word !== '' && isFocused) {
      fetch(process.env.REACT_APP_DICTIONARY_API_URL + '/entries/search/' + word)
      .then(response => response.json())
      .then((data) => {
        setSuggestions(data);
      })
      .catch(err => console.error(err));

      if (!isOpen) setOpen(true);      
    } else if (word === '' || !isFocused) {
      setOpen(false);
    }

    if (referenceElement) {
      if (popperElement) {
        popperElement.style.width = `${referenceElement.offsetWidth}px`;

        var rect = referenceElement.getBoundingClientRect();
        popperElement.style.transform = `translate(${rect.left}px, 53px)`;
      }
    }
  }, [word, isOpen, isFocused, popperElement, referenceElement]);

  return (
    <nav className="navbar sticky-top navbar-light bg-white border-bottom">
      <div className="container-md">
        <form className="form-inline flex-grow-1 mx-2">
          <input className="form-control form-control-lg flex-grow-1 mr-sm-3" ref={setReferenceElement} onFocus={() => setFocused(true)} onBlur={() => setFocused(false)} value={word} onChange={(event) => setWord(event.target.value)} placeholder="Type words to search..." aria-label="Search" />
          <LinkButton to={"/entries/word/" + word} className="btn btn-outline-dark btn-lg ml-auto" onClick={(event) => event.preventDefault()}>Search</LinkButton>

          {(suggestions && suggestions.length !== 0 && isOpen) ? (
            <div className="list-group" ref={setPopperElement} style={styles.popper} {...attributes.popper}>
              {suggestions.map((suggestion) => (
                <Link to={"/entries/word/" + suggestion} className="list-group-item list-group-item-action" key={suggestion} onMouseDown={() => history.push("/entries/word/" + suggestion)}>{word}<b>{suggestion.substring(word.length, suggestion.length)}</b></Link>
              ))}
            </div>
          ) : <div />}
        </form>
      </div>
    </nav>
  );

};

export default withRouter(App);