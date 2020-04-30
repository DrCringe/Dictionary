import React, { Component } from "react";
import { Route, Link, Switch, withRouter } from 'react-router-dom';
import Entries from "./Entries";
import EntryAddEdit from "./EntryAddEdit";


class Dashboard extends Component {

  getAlphabet() {
    let lst = []

    for (let i = 'A'.charCodeAt(); i <= 'Z'.charCodeAt(); i++) {
      let letter = String.fromCharCode(i);

      lst.push(
        <Link to={"/entries/letter/" + letter} className="list-group-item list-group-item-action bg-white text-center px-0 pt-1 pb-0" key={letter}><h4>{letter}</h4></Link>
      );
    }

    return lst;
  }

  buildEntriesRequest(word, letter, page) {
    let entriesRequest = '';

    if (word) {
      entriesRequest = process.env.REACT_APP_DICTIONARY_API_URL + '/entries/word?word=' + word;
      if (page) entriesRequest = entriesRequest.concat('&page=' + (page - 1));
    } else if (letter) {
      entriesRequest = process.env.REACT_APP_DICTIONARY_API_URL + '/entries/letter/' + letter;
      if (page) entriesRequest = entriesRequest.concat('?page=' + (page - 1));
    } else {
      entriesRequest = process.env.REACT_APP_DICTIONARY_API_URL + '/entries';
      if (page) entriesRequest = entriesRequest.concat('?page=' + (page - 1));
    }

    return entriesRequest;
  }

  render() {
    return (
      <div className="container">
        <div className="row">
          <Switch>
            <Route exact path="/entries" render={() => <Entries entriesRequest={this.buildEntriesRequest()} /> }/>
            <Route path="/entries/page/:page" render={({match}) => <Entries entriesRequest={this.buildEntriesRequest(undefined, undefined, match.params.page)} /> }/>
            <Route path="/entries/id/:id" render={({match}) => <EntryAddEdit mode='edit' editRequest={process.env.REACT_APP_DICTIONARY_API_URL + '/entries/' + match.params.id} />} />
            <Route path="/entries/add" render={() => <EntryAddEdit mode='add' />} />
            <Route exact path="/entries/word/:word" render={({match}) => <Entries entriesRequest={this.buildEntriesRequest(match.params.word, undefined, undefined)} />} />
            <Route exact path="/entries/letter/:letter" render={({match}) => <Entries entriesRequest={this.buildEntriesRequest(undefined, match.params.letter, undefined)} />} />
            <Route path="/entries/word/:word/page/:page" render={({match}) => <Entries entriesRequest={this.buildEntriesRequest(match.params.word, undefined, match.params.page)} />} />
            <Route path="/entries/letter/:letter/page/:page" render={({match}) => <Entries entriesRequest={this.buildEntriesRequest(undefined, match.params.letter, match.params.page)} />} />
          </Switch>
          <div className="col-1 pl-2">
            <div className="list-group mr-2 my-sm-2">
              {this.getAlphabet()}
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default withRouter(Dashboard);