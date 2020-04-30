import React, { Component } from 'react'
import '../App.css';
import { Link, withRouter } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import LinkButton from './LinkButton';

class Entries extends Component {

  constructor(props) {

    super(props);

    this.state = {
      request: this.props.entriesRequest,
      entries: null,
      message: null
    };
  }

  componentDidMount() {
    fetch(this.state.request)
      .then(response => {
        if (!response.ok) {
          const headerMessage = response.headers.get('Message');
          this.setState({ message: headerMessage});
        }
        return response.json();
      })
      .then((data) => {
        this.setState({entries: data})
      })
      .catch(err => console.error(err));
  }

  componentDidUpdate(prevProps) {
    if (prevProps.entriesRequest !== this.props.entriesRequest) {
      this.setState({request: this.props.entriesRequest}, () => {this.componentDidMount()});
    }
  }

  async deleteEntry(id) {
    await fetch(process.env.REACT_APP_DICTIONARY_API_URL + '/entries/' + id, { method: 'DELETE' })
    .catch(err => console.error(err));

    if (this.state.entries.content.length === 1) {
      this.props.history.push("/entries");
    }
    this.componentDidMount();
  }

  getPagination() {
    let pages = [];
    const neighbors = 3;
    const currentPage = this.state.entries.pageable.pageNumber;
    const totalPages = this.state.entries.totalPages;

    const hasLeftSpan = (currentPage > neighbors + 1) && (totalPages > 2 * neighbors + 1);
    const hasRightSpan = (currentPage < totalPages - neighbors - 1) && (totalPages > 2 * neighbors + 1);

    let routeUrl = this.props.location.pathname;
    if (routeUrl.includes('/page/')) {
      routeUrl = routeUrl.slice(0, routeUrl.indexOf('/page/'));
    }

    pages.push(
      <li className={`page-item${this.state.entries.first ? ' disabled' : ''}`} key="L">
        <Link to={routeUrl + '/page/' + currentPage} className="page-link" aria-label="Previous">
          <span aria-hidden="true">&laquo;</span>
          <span className="sr-only">Previous</span>
        </Link>
      </li>
    );

    if (hasLeftSpan) {
      pages.push(
        <li className="page-item" key="1">
          <Link to={routeUrl + "/page/1"} className="page-link">1</Link>
        </li>,
        <li className="page-item disabled" key="SL">
          <Link to="" className="page-link"><span aria-hidden="true">&hellip;</span></Link>
        </li>
      )
    }

    for (let i = (hasLeftSpan ? currentPage - neighbors : 0); i < totalPages; i++) {
      if (i < currentPage + neighbors + 1) {
        pages.push(
          <li className={`page-item${i === currentPage ? ' active' : ''}`} key={i + 1}>
            <Link to={routeUrl + "/page/" + (i + 1)} className="page-link">{i + 1}</Link>
          </li>
        );
      }
    }

    if (hasRightSpan) {
      pages.push(
        <li className="page-item disabled" key="SR">
          <Link to="" className="page-link"><span aria-hidden="true">&hellip;</span></Link>
        </li>,
        <li className="page-item" key={totalPages}>
          <Link to={routeUrl + "/page/" + totalPages} className="page-link">{totalPages}</Link>
        </li>
      )
    }

    pages.push(
      <li className={`page-item${this.state.entries.last ? ' disabled' : ''}`} key="R">
        <Link to={routeUrl + '/page/' + (currentPage + 2)} className="page-link" aria-label="Next">
          <span aria-hidden="true">&raquo;</span>
          <span className="sr-only">Next</span>
        </Link>
      </li>
    );

    return pages;
  }

  render() {
    if (this.state.entries) {
      if (this.state.entries.content[0].id) {
        return (
          <div className="col-11 pr-0">
            {this.state.entries.content.map((entry) => (
              <div className="card ml-2 py-2 px-3 my-sm-2 border-0 shadow-sm" key={entry.id}>
                <div className="row">
                  <div className="col-md-10">
                    <h4 className="card-title">{entry.word}</h4>
                  </div>
                  <div className="col-md-2">
                    <div className="d-flex flex-row-reverse">
                      <LinkButton to={"/entries/id/" + entry.id} type="button" className="btn btn-default btn-lg p-0 ml-sm-3">
                        <FontAwesomeIcon icon="edit" />
                      </LinkButton>
                      <button type="button" className="btn btn-default btn-lg p-0" onClick={(event) => this.deleteEntry(entry.id, event)}>
                        <FontAwesomeIcon icon="trash-alt" />
                      </button>
                    </div>
                  </div>
                </div>
                <h5 className="card-subtitle mb-2 text-muted">{entry.wordtype}</h5>
                <p className="card-text">{entry.definition}</p>
              </div>
            ))}
            <nav aria-label="Page navigation example my-sm-2">
              <ul className="pagination justify-content-center">
                {this.getPagination()}
              </ul>
            </nav>
          </div>
        );
      } else if (this.state.message) {
        return (
          <div className="col-11 pr-0">
            <form className="bg-white rounded shadow-sm ml-2 my-sm-2 py-3 px-4">
              <div className="d-flex justify-content-left align-items-center" id="main">
                <h1 className="mr-3 pr-3 align-top border-right inline-block align-content-center">404</h1>
                <div className="inline-block align-middle">
                  <h2 className="font-weight-normal lead" id="desc">{this.state.message}</h2>
                </div>
              </div>

              {this.state.entries.content && 
                <div>
                  <h2 className="font-weight-light py-2">Perhabs, you mean:</h2>
                  {this.state.entries.content.map((alternative) => (
                    <Link to={"/entries/word/" + alternative} key={alternative}><h5 className="ml-4 my-sm-2">{alternative}</h5></Link>
                  ))}
                </div>
              }
            </form>
          </div>
        );
      } else {
        return (
          <div>sdfasdfsakdljfh</div>
        );
      }
    } else {
      return (
        <div />
      );
    }
  }
}

export default withRouter(Entries);