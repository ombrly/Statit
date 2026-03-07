// Commands to run for just running frontend on its own:
// npm install
// npm run dev 
// Then open http://localhost:5173 in your browser to see the app. 

import React, { useState } from 'react';

// Reusable style to keep code clean
const centeredpageStyle = {
  display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
  minHeight: '100vh', width: '100vw', fontFamily: 'sans-serif', padding: '20px',
  boxSizing: 'border-box', textAlign: 'center', position: 'relative',
  backgroundColor: '#fdfdfd'
};

// 1. Define your "Pages" as mini-components
const HomePage = ({ setPage }) => (
  //<div style ={{ textAlign: 'center' }}>
  <div style={centeredPageStyle}>
    <h1>Welcome to Global Ranking System!</h1>
    <button onClick={() => setPage('GlobalRankingPage')}>Go to Global Data</button>
    <button onClick={() => setPage('LocalRankingPage')}>Go to Local Data</button>
  </div>
);

const GlobalRankingPage = ({ setPage}) => (
  <div style={centeredPageStyle}>
    <h1> Global data </h1>
    <button onClick={() => setPage('home')}>Back to Home</button>
  </div>
);

const LocalRankingPage = ({ setPage}) => (
    <div style={centeredPageStyle}>
    <h1> Local data </h1>
    <button onClick={() => setPage('home')}>Back to Home</button>
  </div>
);

// 2. Main App component that manages which "Page" to show
const App = () => {
  const [currentPage, setCurrentPage] = useState('home'); // State to track current page
  // Function to render the current page based on state
  const renderPage = () => {
    switch (currentPage) {
      case 'home':
        return <HomePage setPage={setCurrentPage} />;
      case 'GlobalRankingPage':
        return <GlobalRankingPage setPage={setCurrentPage} />;
      case 'LocalRankingPage':
        return <LocalRankingPage setPage={setCurrentPage} />;
      default:
        return <HomePage setPage={setCurrentPage} />;
    }
  };
  return (<div>
    {renderPage()}
  </div>);
};

export default App;