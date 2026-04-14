// Commands to run for just running frontend on its own:
// npm install
// npm run dev 
// Then open http://localhost:5173 in your browser to see the app. 

import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useParams, useNavigate, Navigate, useLocation } from 'react-router-dom';

// --- HELPERS ---
const getStorage = (key, defaultValue) => {
  const saved = localStorage.getItem(key);
  return saved ? JSON.parse(saved) : defaultValue;
};

// --- SHARED STYLES ---
const pageWrapperStyle = {
  display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
  minHeight: '100vh', width: '100vw', fontFamily: 'sans-serif', padding: '20px',
  boxSizing: 'border-box', textAlign: 'center', position: 'relative',
  backgroundColor: '#fdfdfd'
};

// Added zIndex: 10 so it doesn't get hidden behind the page wrapper!
const topNavStyle = {
  position: 'absolute', top: '20px', right: '20px', display: 'flex', gap: '10px', alignItems: 'center', zIndex: 10
};

const backLinkStyle = { position: 'absolute', top: '20px', left: '20px', textDecoration: 'none', color: '#8b5cf6', fontWeight: 'bold', fontSize: '1.1rem', zIndex: 10 };
const inputStyle = { padding: '12px', borderRadius: '8px', border: '1px solid #ccc', width: '280px', marginBottom: '15px', fontSize: '1rem' };

const mainButtonStyle = { 
  padding: '15px 20px', cursor: 'pointer', borderRadius: '8px', border: '2px solid #8b5cf6',
  backgroundColor: '#fff', fontSize: '1.4rem', width: '380px', fontWeight: 'bold', transition: '0.2s', color: '#333'
};

const smallButtonStyle = {
  padding: '10px 18px', cursor: 'pointer', borderRadius: '6px', border: '2px solid #8b5cf6',
  backgroundColor: '#fff', fontSize: '1rem', fontWeight: 'bold', color: '#333', transition: '0.2s'
};

const funTitleStyle = {
  fontSize: '3.2rem', 
  marginBottom: '40px', 
  color: '#2c3e50',
  fontFamily: '"Comic Sans MS", "Chalkboard SE", "Marker Felt", cursive'
};

// --- MAIN APP ---
export default function App() {
  const defaultPresets = [
    { name: "Wealth", better: "large", unit: "$", type: "global" },
    { name: "Health", better: "large", unit: "pts", type: "global" },
    { name: "Speed", better: "small", unit: "sec", type: "global" }
  ];

  const [categories, setCategories] = useState(() => getStorage('categories', defaultPresets));
  const [allStats, setAllStats] = useState(() => getStorage('allStats', {}));
  const [users, setUsers] = useState(() => getStorage('users', []));
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    localStorage.setItem('categories', JSON.stringify(categories));
    localStorage.setItem('allStats', JSON.stringify(allStats));
    localStorage.setItem('users', JSON.stringify(users));
    localStorage.removeItem('currentUser');
  }, [categories, allStats, users]);

  return (
    <Router>
      <AppContent 
        categories={categories} setCategories={setCategories}
        allStats={allStats} setAllStats={setAllStats}
        users={users} setUsers={setUsers}
        currentUser={currentUser} setCurrentUser={setCurrentUser}
      />
    </Router>
  );
}

// --- APP CONTENT ---
export default function App() {
  const [categories, setCategories] = useState([]);
  const [currentUser, setCurrentUser] = useState(() => getStorage('currentUser', null));

  // Save user session
  useEffect(() => {
    sessionStorage.setItem('currentUser', JSON.stringify(currentUser));
  }, [currentUser]);

  // Fetch categories from your Azure Database on load
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const data = await getAllCategories();
        const categoryList = data.categories || data.content || data; 
        setCategories(categoryList);
      } catch (err) {
        console.error("Failed to load categories:", err);
      }
    };
    fetchCategories();
  }, []);

  // THIS is what was missing! React needs to know what to draw.
  return (
    <Router>
      <AppContent 
        categories={categories} 
        setCategories={setCategories}
        currentUser={currentUser} 
        setCurrentUser={setCurrentUser}
      />
    </Router>
  );
}

// --- HOME PAGE ---
const Home = () => (
  <div style={pageWrapperStyle}>
    <h1 style={funTitleStyle}>WELCOME TO GLOBAL RANKING SYSTEM! 🏆</h1>
    
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', marginBottom: '40px' }}>
      <Link to="/global"><button style={mainButtonStyle}>Global Category</button></Link>
      <Link to="/create"><button style={mainButtonStyle}>Create Your Own</button></Link>
      <Link to="/created"><button style={mainButtonStyle}>See Created Categories</button></Link>
    </div>
  </div>
);

// --- PROFILE PAGE ---
const ProfilePage = ({ currentUser, setUsers, setCurrentUser }) => {
  const toggleAnonymous = () => {
    const updatedUser = { ...currentUser, isAnonymous: !currentUser.isAnonymous };
    setCurrentUser(updatedUser);
    setUsers(prevUsers => prevUsers.map(u => u.username === updatedUser.username ? updatedUser : u));
  };

  return (
    <div style={pageWrapperStyle}>
      <Link to="/" style={backLinkStyle}>← Back to Main</Link>
      <h2 style={{ fontSize: '2.5rem', marginBottom: '30px' }}>Your Profile</h2>
      <div style={{ backgroundColor: '#fff', padding: '40px', borderRadius: '20px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '20px', minWidth: '350px' }}>
        <p style={{ fontSize: '1.4rem', margin: 0 }}><strong>Username:</strong> {currentUser.username}</p>
        <div style={{ borderTop: '1px solid #eee', width: '100%', margin: '10px 0' }}></div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <span style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>Anonymous Mode:</span>
          <button 
            onClick={toggleAnonymous}
            style={{ ...smallButtonStyle, backgroundColor: currentUser.isAnonymous ? '#8b5cf6' : '#fff', color: currentUser.isAnonymous ? '#fff' : '#333', width: '80px' }}
          >
            {currentUser.isAnonymous ? 'ON' : 'OFF'}
          </button>
        </div>
        <p style={{ color: '#666', fontSize: '1rem', maxWidth: '280px', margin: 0 }}>
          {currentUser.isAnonymous ? "Your new entries will be hidden and appear as 'Anonymous' on the leaderboards." : "Your new entries will publicly display your username on the leaderboards."}
        </p>
      </div>
    </div>
  );
};

// --- LOGIN / SIGNUP PAGE ---
const AuthPage = ({ mode, users, setUsers, setCurrentUser }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleAuth = (e) => {
    e.preventDefault();
    if (mode === 'signup') {
      if (users.find(u => u.username === username)) return alert("Username already taken!");
      const newUser = { username, password, isAnonymous: false };
      setUsers([...users, newUser]);
      setCurrentUser(newUser);
      navigate('/');
    } else {
      const user = users.find(u => u.username === username && u.password === password);
      if (user) {
        setCurrentUser(user);
        navigate('/');
      } else {
        alert("Incorrect username or password. Please try again.");
      }
    }
  };

  return (
    <div style={pageWrapperStyle}>
      <h1 style={{ ...funTitleStyle, marginBottom: '10px' }}>GLOBAL RANKING SYSTEM 🏆</h1>
      <h2 style={{ fontSize: '1.8rem', marginBottom: '40px', color: '#666' }}>
        {mode === 'login' ? 'Log in to continue' : 'Create an account'}
      </h2>

      <form onSubmit={handleAuth} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', backgroundColor: '#fff', padding: '40px', borderRadius: '20px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)', marginBottom: '20px' }}>
        <input style={inputStyle} placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} required />
        <input style={inputStyle} type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} required />
        <button type="submit" style={{ ...mainButtonStyle, width: '280px', fontSize: '1.2rem', backgroundColor: '#8b5cf6', color: 'white' }}>
          {mode === 'login' ? 'Log in' : 'Create account'}
        </button>
      </form>

      {mode === 'login' ? (
        <p style={{ color: 'black' }}>Don't have an account? <Link to="/signup" style={{ color: '#8b5cf6', fontWeight: 'bold' }}>Create one here</Link></p>
      ) : (
        <p style={{ color: 'black' }}>Already have an account? <Link to="/login" style={{ color: '#8b5cf6', fontWeight: 'bold' }}>Log in here</Link></p>
      )}
    </div>
  );
};

// --- CATEGORY LIST PAGE ---
const CategoryList = ({ title, categories }) => (
  <div style={pageWrapperStyle}>
    <Link to="/" style={backLinkStyle}>← Back to Main</Link>
    <h2 style={{ fontSize: '2rem', marginBottom: '30px' }}>{title}</h2>
    {categories.length === 0 ? (
      <div style={{ fontSize: '1.2rem' }}>
        <p>No categories here yet.</p>
        {title.includes("Created") && <Link to="/create" style={{ color: '#8b5cf6' }}>Create one now!</Link>}
      </div>
    ) : (
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px', maxWidth: '900px' }}>
        {categories.map(cat => (
          <Link key={cat.name} to={`/ranking/${cat.name}`} style={{ 
            width: '160px', height: '160px', border: '2px solid #8b5cf6', display: 'flex', alignItems: 'center', justifyContent: 'center', 
            textDecoration: 'none', color: 'black', fontWeight: 'bold', borderRadius: '15px', backgroundColor: '#fff', fontSize: '1.1rem', boxShadow: '0 4px 10px rgba(0,0,0,0.05)'
          }}>{cat.name}</Link>
        ))}
      </div>
    )}
  </div>
);

// --- CREATE CATEGORY PAGE ---
const CreateCategory = ({ setCategories }) => {
  const [name, setName] = useState("");
  const [better, setBetter] = useState("large");
  const [unit, setUnit] = useState("");
  const navigate = useNavigate();

  const handleCreate = (e) => {
    e.preventDefault();
    if (!name || !unit) return;
    setCategories(prev => [...prev, { name, better, unit, type: "user" }]);
    navigate('/created');
  };

  return (
    <div style={pageWrapperStyle}>
      <Link to="/" style={backLinkStyle}>← Back</Link>
      <h2 style={{ fontSize: '2rem' }}>Design Your Ranking</h2>
      <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '20px', alignItems: 'center', backgroundColor: '#fff', padding: '40px', borderRadius: '20px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)' }}>
        <div style={{ textAlign: 'left', width: '100%' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Category Name</label>
          <input style={inputStyle} placeholder="e.g. Typing Speed" value={name} onChange={e => setName(e.target.value)} required />
        </div>
        <div style={{ textAlign: 'left', width: '100%' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Measurement Unit</label>
          <input style={inputStyle} placeholder="e.g. WPM" value={unit} onChange={e => setUnit(e.target.value)} required />
        </div>
        <div>
          <p style={{ fontWeight: 'bold', marginBottom: '10px' }}>Which is better?</p>
          <button type="button" onClick={() => setBetter('large')} style={{ ...mainButtonStyle, width: 'auto', fontSize: '1rem', padding: '10px 15px', backgroundColor: better === 'large' ? '#8b5cf6' : '#fff', color: better === 'large' ? '#fff' : '#000' }}>Large Number</button>
          <button type="button" onClick={() => setBetter('small')} style={{ ...mainButtonStyle, width: 'auto', fontSize: '1rem', padding: '10px 15px', marginLeft: '10px', backgroundColor: better === 'small' ? '#8b5cf6' : '#fff', color: better === 'small' ? '#fff' : '#000' }}>Small Number</button>
        </div>
        <button type="submit" style={{ ...mainButtonStyle, width: '100%', backgroundColor: '#4CAF50', color: 'white', border: 'none' }}>Create Category</button>
      </form>
    </div>
  );
};

// --- RANKING PAGE ---
const RankingPage = ({ categories, allStats, setAllStats, currentUser }) => {
  const { categoryName } = useParams();
  const catInfo = categories.find(c => c.name === categoryName) || { better: 'large', unit: '', type: 'global' };
  const currentStats = allStats[categoryName] || [];
  
  // Input form states
  const [val, setVal] = useState("");
  const [gender, setGender] = useState("Male");
  const [region, setRegion] = useState("North America");
  
  // View controls
  const [viewMode, setViewMode] = useState("Global"); // 'Global', 'Gender', 'Region'
  const displayName = currentUser?.isAnonymous ? "Anonymous" : currentUser?.username;

  const addEntry = (e) => {
    e.preventDefault();
    if (!val) return;
    
    // Save entry with tagged region and gender
    const newEntry = { name: displayName, value: parseFloat(val), gender, region };
    const updated = [...currentStats, newEntry]; 
    
    setAllStats(prev => ({ ...prev, [categoryName]: updated }));
    setVal(""); 
  };

  const getRankDisplay = (i) => i === 0 ? "1st 🥇" : i === 1 ? "2nd 🥈" : i === 2 ? "3rd 🥉" : `${i + 1}th`;

  // Reusable function to render standard tables based on filtered data
  const renderTable = (title, statsToRender) => {
    // Sort and grab top 100 for THIS specific view
    const sorted = [...statsToRender]
      .sort((a, b) => catInfo.better === "large" ? b.value - a.value : a.value - b.value)
      .slice(0, 100);

    return (
      <div key={title} style={{ width: '100%', maxWidth: '400px', maxHeight: '500px', overflowY: 'auto', border: '1px solid #eee', borderRadius: '12px', backgroundColor: '#fff', boxShadow: '0 5px 15px rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column' }}>
        <h3 style={{ margin: '15px 0', fontSize: '1.4rem', color: '#8b5cf6' }}>{title} Table</h3>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '1.1rem' }}>
          <thead style={{ position: 'sticky', top: 0, backgroundColor: '#8b5cf6', color: 'white', zIndex: 1 }}>
            <tr><th style={{ padding: '12px' }}>Rank</th><th>{catInfo.unit || 'Score'}</th><th>Name</th></tr>
          </thead>
          <tbody>
            {sorted.length === 0 ? (
              <tr><td colSpan="3" style={{ padding: '20px', color: '#666' }}>No entries yet.</td></tr>
            ) : (
              sorted.map((stat, i) => (
                <tr key={i} style={{ backgroundColor: i < 3 ? '#fff9e6' : '#fff', borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '12px', fontWeight: i < 3 ? 'bold' : 'normal' }}>{getRankDisplay(i)}</td>
                  <td>{stat.value ?? "-"}</td>
                  <td style={{ fontWeight: i < 3 ? 'bold' : 'normal', fontStyle: stat.name === 'Anonymous' ? 'italic' : 'normal' }}>{stat.name ?? "-"}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    );
  };

  return (
    <div style={{ ...pageWrapperStyle, justifyContent: 'flex-start', paddingTop: '80px' }}>
      <Link to={catInfo.type === 'global' ? "/global" : "/created"} style={backLinkStyle}>← Back</Link>
      <h2 style={{ textTransform: 'capitalize', fontSize: '2.5rem', marginBottom: '20px' }}>{categoryName} Rankings</h2>
      
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '30px', justifyContent: 'center', marginBottom: '40px' }}>
        
        {/* SUBMISSION FORM WITH DEMOGRAPHICS */}
        <form onSubmit={addEntry} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '15px', backgroundColor: '#fff', padding: '25px', borderRadius: '15px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)' }}>
          <h3 style={{ margin: 0, fontSize: '1.3rem' }}>Submit Your Stat</h3>
          
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <input type="number" value={val} onChange={e => setVal(e.target.value)} placeholder="0" style={{ width: '100px', height: '60px', textAlign: 'center', border: '2px solid #8b5cf6', fontSize: '24px', borderRadius: '10px' }} required />
            <span style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>{catInfo.unit}</span>
          </div>

          <select style={{ ...inputStyle, marginBottom: '0', width: '220px' }} value={gender} onChange={e => setGender(e.target.value)}>
            <option value="Male">Male</option>
            <option value="Female">Female</option>
          </select>

          <select style={{ ...inputStyle, marginBottom: '0', width: '220px' }} value={region} onChange={e => setRegion(e.target.value)}>
            <option value="North America">North America</option>
            <option value="South America">South America</option>
            <option value="Europe">Europe</option>
            <option value="Africa">Africa</option>
            <option value="Asia">Asia</option>
            <option value="Australia/Oceania">Australia/Oceania</option>
            <option value="Antarctica">Antarctica</option>
          </select>

          <input style={{ ...inputStyle, width: '220px', textAlign: 'center', backgroundColor: '#f3f4f6', color: '#666', cursor: 'not-allowed', marginBottom: 0 }} value={displayName} readOnly title="Change this in your Profile" />
          
          <button type="submit" style={{ ...mainButtonStyle, width: '220px', fontSize: '1.1rem', backgroundColor: '#8b5cf6', color: 'white' }}>Add Entry</button>
        </form>

        {/* VIEW MODE SELECTOR */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '15px', backgroundColor: '#fff', padding: '25px', borderRadius: '15px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)' }}>
          <h3 style={{ margin: 0, fontSize: '1.3rem' }}>View Leaderboards</h3>
          <select style={{ ...inputStyle, border: '2px solid #8b5cf6', fontSize: '1.2rem', width: '220px', padding: '15px' }} value={viewMode} onChange={e => setViewMode(e.target.value)}>
            <option value="Global">🌎 Global Ranking</option>
            <option value="Gender">🚻 By Gender</option>
            <option value="Region">🗺️ By Region</option>
          </select>
          <p style={{ color: '#666', maxWidth: '200px', fontSize: '0.95rem' }}>
            Select a filter to dynamically split the leaderboards below!
          </p>
        </div>

      </div>

      {/* DYNAMIC TABLES CONTAINER */}
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '25px', width: '100%', maxWidth: '1400px' }}>
        {viewMode === 'Global' && renderTable('Global', currentStats)}
        
        {viewMode === 'Gender' && [
          renderTable('Male', currentStats.filter(s => s.gender === 'Male')),
          renderTable('Female', currentStats.filter(s => s.gender === 'Female'))
        ]}
        
        {viewMode === 'Region' && [
          renderTable('North America', currentStats.filter(s => s.region === 'North America')),
          renderTable('South America', currentStats.filter(s => s.region === 'South America')),
          renderTable('Europe', currentStats.filter(s => s.region === 'Europe')),
          renderTable('Africa', currentStats.filter(s => s.region === 'Africa')),
          renderTable('Asia', currentStats.filter(s => s.region === 'Asia')),
          renderTable('Australia/Oceania', currentStats.filter(s => s.region === 'Australia/Oceania')),
          renderTable('Antarctica', currentStats.filter(s => s.region === 'Antarctica'))
        ]}
      </div>
    </div>
  );
};
