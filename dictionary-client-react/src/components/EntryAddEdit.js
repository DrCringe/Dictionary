import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import LinkButton from "./LinkButton";

class EntryAddEdit extends Component {

  constructor(props) {

    super(props);

    this.state = {
      request: this.props.editRequest,
      mode: this.props.mode,
      entry: null,

      word: '',
      wordtype: '',
      definition: '',

      status: 200,
      message: '',
      placeholder: ''
    };

    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleWordChange = this.handleWordChange.bind(this);
    this.handleWordtypeChange = this.handleWordtypeChange.bind(this);
    this.handleDefinitionChange = this.handleDefinitionChange.bind(this);
  }

  componentDidMount() {
    if (this.state.mode === 'edit') {
      fetch(this.state.request)
      .then(response => response.json())
      .then((data) => {
        this.setState({
          entry: data,
          word: data.word,
          wordtype: data.wordtype,
          definition: data.definition
        })
      })
      .catch(error => console.log(error));
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.editRequest !== prevProps.editRequest || this.props.mode !== prevProps.mode) {
      this.setState({request: this.props.editRequest}, () => this.componentDidMount());
    }
  }

  handleWordChange(event) {
    this.setState({word: event.target.value});
  }

  handleWordtypeChange(event) {
    this.setState({wordtype: event.target.value});
  }

  handleDefinitionChange(event) {
    this.setState({definition: event.target.value});
  }

  async addEntry() {
    await fetch(process.env.REACT_APP_DICTIONARY_API_URL + '/entries', {
      method: 'POST',
      headers: { 'Content-type': 'application/json' },
      body: JSON.stringify({
        word: this.state.word,
        wordtype: this.state.wordtype,
        definition: this.state.definition
      })
    })
    .then(response => {
      this.setState({status: response.status});
      if (!response.ok) {
        const headerMessage = response.headers.get('Message');
        this.setState({ message: headerMessage});
        return;
      }
      return response.json();
    })
    .then((data) => {
      this.setState({entry: data})
    })
    .catch(err => console.error(err));
  }

  async editEntry() {
    await fetch(process.env.REACT_APP_DICTIONARY_API_URL + '/entries/' + this.state.entry.id, {
      method: 'PUT',
      headers: { 'Content-type': 'application/json' },
      body: JSON.stringify({
        word: this.state.word,
        wordtype: this.state.wordtype,
        definition: this.state.definition
      })
    })
    .catch(err => console.error(err));
  }

  async editDefiniton() {
    await fetch(process.env.REACT_APP_DICTIONARY_API_URL + '/entries/' + this.state.entry.id + '?newDefinition=' + this.state.definition, {
      method: 'PATCH'
    })
    .catch(err => console.error(err));
  }

  async handleSubmit(event) {
    event.preventDefault();

    if (this.state.mode === 'edit') {
      if (this.state.word === '' || this.state.wordtype === '' || this.state.definition === '') {
        this.setState({placeholder: "This field can not be empty!"});
        return;
      }
      if (this.state.entry.word !== this.state.word || this.state.entry.wordtype !== this.state.wordtype) {
          await this.editEntry();
      } else if (this.state.entry.definiton !== this.state.definition) {
        await this.editDefiniton();
      }
    } else if (this.state.mode === 'add') {
      if (this.state.word && this.state.wordtype && this.state.definition) {
        await this.addEntry();
      } else {
        this.setState({placeholder: "This field can not be empty!"});
        return;
      }
    }

    if (this.state.status - 399 < 0) {
      this.props.history.push("/entries/word/" + this.state.word);
    }
  }

  render() {
    return (this.state.entry || this.state.mode === 'add') ? (
      <div className="col-11 pr-0">
        {this.state.status - 399 > 0 && 
          <div className="alert alert-danger ml-2 mt-sm-2" role="alert">
              <div className="d-flex justify-content-left align-items-center" id="main">
                <h1 className="mr-3 mb-0 pr-3 align-top border-right border-dark inline-block align-content-center">{this.state.status}</h1>
                <div className="inline-block align-middle">
                  <h2 className="mb-0 font-weight-normal lead" id="desc">{this.state.message}</h2>
                </div>
              </div>
          </div>
        }

        <form className="bg-white rounded shadow-sm ml-2 my-sm-2 py-3 px-4">
          <h3>{this.state.mode === 'add' ? 'Add entry' : 'Edit entry'}</h3>
          <div className="form-group">
            <label htmlFor="entryWord"><h5>Word</h5></label>
            <input type="text" className={(this.state.placeholder !== '' && this.state.word === '') ? "form-control form-control-lg is-invalid" : "form-control form-control-lg"} id="entryWord" placeholder={this.state.placeholder} value={this.state.word} onChange={this.handleWordChange} />
          </div>
          <div className="form-group">
            <label htmlFor="entryWordType"><h5>Word type</h5></label>
            <input type="text" className={(this.state.placeholder !== '' && this.state.wordtype === '') ? "form-control form-control-lg is-invalid" : "form-control form-control-lg"} id="entryWordType" placeholder={this.state.placeholder} value={this.state.wordtype} onChange={this.handleWordtypeChange} />
          </div>
          <div className="form-group">
            <label htmlFor="entryDefinition"><h5>Definition</h5></label>
            <textarea rows="8" className={(this.state.placeholder !== '' && this.state.definition === '') ? "form-control form-control-lg is-invalid" : "form-control form-control-lg"} id="entryDefinition" placeholder={this.state.placeholder} value={this.state.definition} onChange={this.handleDefinitionChange} />
          </div>
          <div className="d-flex flex-row-reverse">
            <LinkButton to="#" className="btn btn-outline-dark btn-lg ml-sm-3" onClick={this.handleSubmit}>Submit</LinkButton>
            <LinkButton to={(this.state.mode === 'edit') ? "/entries/word/" + this.state.entry.word : "/entries"} className="btn btn-outline-dark btn-lg">Cancel</LinkButton>
          </div>
        </form>
      </div>
    ) : (<div></div>);
  }

}

export default withRouter(EntryAddEdit);