import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useParams, useNavigate, Navigate } from 'react-router-dom';

const API_BASE_URL = 'http://statit-backend-api.eastus.azurecontainer.io:8080/api/v1';

// --- HELPERS ---
const getStorage = (key, defaultValue) => {
  const saved = localStorage.getItem(key);
  return saved ? JSON.parse(saved) : defaultValue;
};

const getUserByUsername = async (username) => {
  const response = await fetch(`${API_BASE_URL}/users/${username}`);
  if (!response.ok) throw new Error('User not found');
  return response.json();
};

const createCategory = async (categoryData) => {
  const response = await fetch(`${API_BASE_URL}/categories`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(categoryData),
  });
  if (!response.ok) {
    const errText = await response.text();
    throw new Error(errText);
  }
  return response.json();
};

const getAllCategories = async (page = 0, size = 25) => {
  const response = await fetch(`${API_BASE_URL}/categories?page=${page}&size=${size}`);
  if (!response.ok) throw new Error('Failed to fetch categories');
  return response.json();
};

const submitScore = async (scoreData) => {
  const response = await fetch(`${API_BASE_URL}/scores`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(scoreData),
  });
  if (!response.ok) {
    const errText = await response.text();
    throw new Error(errText);
  }
  return response.json();
};

const getTopScores = async (categoryId, page = 0, size = 25) => {
  const response = await fetch(`${API_BASE_URL}/leaderboards/${categoryId}/top?page=${page}&size=${size}`);
  if (!response.ok) throw new Error('Failed to fetch leaderboard');
  return response.json();
};

const getStorage = (key, defaultValue) => {
  const saved = sessionStorage.getItem(key);
  return saved ? JSON.parse(saved) : defaultValue;
};

const pageWrapperStyle = { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', width: '100vw', fontFamily: 'sans-serif', padding: '20px', paddingTop: '100px', boxSizing: 'border-box', textAlign: 'center', position: 'relative', backgroundColor: '#fdfdfd' };
const topNavStyle = { position: 'fixed', top: 0, left: 0, width: '100vw', height: '75px', backgroundColor: '#fff', boxShadow: '0 2px 10px rgba(0,0,0,0.1)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 30px', boxSizing: 'border-box', zIndex: 1000 };
const logoStyle = { height: '50px', width: 'auto', cursor: 'pointer' };
const navLinksStyle = { display: 'flex', gap: '15px', alignItems: 'center' };
const backLinkStyle = { position: 'absolute', top: '100px', left: '20px', textDecoration: 'none', color: '#8b5cf6', fontWeight: 'bold', fontSize: '1.1rem', zIndex: 10 };
const inputStyle = { padding: '12px', borderRadius: '8px', border: '1px solid #ccc', width: '280px', marginBottom: '15px', fontSize: '1rem' };
const mainButtonStyle = { padding: '15px 20px', cursor: 'pointer', borderRadius: '8px', border: '2px solid #8b5cf6', backgroundColor: '#fff', fontSize: '1.4rem', width: '380px', fontWeight: 'bold', transition: '0.2s', color: '#333' };
const smallButtonStyle = { padding: '10px 18px', cursor: 'pointer', borderRadius: '6px', border: '2px solid #8b5cf6', backgroundColor: '#fff', fontSize: '1rem', fontWeight: 'bold', color: '#333', transition: '0.2s' };
const funTitleStyle = { fontSize: '3.2rem', marginBottom: '40px', color: '#2c3e50', fontFamily: '"Comic Sans MS", "Chalkboard SE", "Marker Felt", cursive' };

export default function App() {
  const defaultPresets = [
    { name: "Wealth", better: "large", unit: "$", type: "global" },
    { name: "Health", better: "large", unit: "pts", type: "global" },
    { name: "Speed", better: "small", unit: "sec", type: "global" }
  ];
  const [categories, setCategories] = useState(getStorage("categories", defaultPresets));
  const [allStats, setAllStats] = useState(getStorage("allStats", {}));
  const [users, setUsers] = useState(getStorage("users", []));
  const [currentUser, setCurrentUser] = useState(getStorage("currentUser", null));
  
  const handleLogin = async (username, password) => {
  try {
    const response = await fetch('/api/login', {
      method: 'POST', // We are sending data
      headers: {
        'Content-Type': 'application/json', // Telling the backend we are sending JSON
      },
      body: JSON.stringify({ username, password }) // The actual data
    });

    if (response.ok) {
      const userData = await response.json();
      setCurrentUser(userData); // Log the user in on the frontend
    } else {
      alert("Invalid credentials!");
    }
  } catch (error) {
    console.error("Login failed:", error);
  }
};
}

const AppContent = ({ categories, setCategories, currentUser, setCurrentUser }) => {
  const navigate = useNavigate();

  const logout = () => {
    setCurrentUser(null);
    sessionStorage.removeItem('currentUser');
    navigate('/login');
  };

  const isCatLocal = (cat) => {
    let t = [];
    if (typeof cat.tags === 'string') {
      try { t = JSON.parse(cat.tags); } catch (e) {}
    } else if (Array.isArray(cat.tags)) {
      t = cat.tags;
    }
    return t.includes('local');
  };

  const globalCategories = categories.filter(c => !isCatLocal(c));
  const localCategories = categories.filter(c => isCatLocal(c));

  return (
    <>
      <div style={topNavStyle}>
        <Link to="/">
          <img src="/logo.webp" alt="Statit Logo" style={logoStyle} />
        </Link>
        <div style={navLinksStyle}>
          <Link to="/global"><button className="nav-button">Global Categories</button></Link>
          <Link to="/local"><button className="nav-button">Local Categories</button></Link>
          {currentUser && <Link to="/profile"><button className="nav-button">Profile</button></Link>}
          {currentUser ? (
            <button onClick={logout} className="nav-button">Log out</button>
          ) : (
            <Link to="/login"><button className="nav-button">Login</button></Link>
          )}
        </div>
      </div>

      <Routes>
        <Route path="/" element={currentUser ? <Home /> : <Navigate to="/login" />} />
        <Route path="/login" element={currentUser ? <Navigate to="/" /> : <AuthPage mode="login" setCurrentUser={setCurrentUser} />} />
        <Route path="/signup" element={currentUser ? <Navigate to="/" /> : <AuthPage mode="signup" setCurrentUser={setCurrentUser} />} />
        <Route path="/profile" element={currentUser ? <ProfilePage currentUser={currentUser} setCurrentUser={setCurrentUser} /> : <Navigate to="/login" />} />
        
        <Route path="/global" element={<CategoryList title="Global Categories" categories={globalCategories} />} />
        <Route path="/local" element={<CategoryList title="Local Categories" categories={localCategories} />} />
        <Route path="/create" element={currentUser ? <CreateCategory currentUser={currentUser} setCategories={setCategories} /> : <Navigate to="/login" />} />
        <Route path="/ranking/:categoryId" element={<RankingPage categories={categories} currentUser={currentUser} />} />
      </Routes>
    </>
  );
};

const Home = () => (
  <div style={pageWrapperStyle}>
    <h1 style={funTitleStyle}>Welcome to Statit!</h1>
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', marginBottom: '40px' }}>
      <Link to="/global"><button style={mainButtonStyle}>Global Categories</button></Link>
      <Link to="/local"><button style={mainButtonStyle}>Local Categories</button></Link>
      <Link to="/create"><button style={mainButtonStyle}>Create Your Own</button></Link>
    </div>
  </div>
);

const ProfilePage = ({ currentUser, setCurrentUser }) => {
  return (
    <div style={pageWrapperStyle}>
      <Link to="/" style={backLinkStyle}>← Back to Main</Link>
      <h2 style={{ fontSize: '2.5rem', marginBottom: '30px' }}>Your Profile</h2>
      <div style={{ backgroundColor: '#fff', padding: '40px', borderRadius: '20px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '20px', minWidth: '350px' }}>
        <p style={{ fontSize: '1.4rem', margin: 0 }}><strong>Username:</strong> {currentUser.username}</p>
        <p style={{ fontSize: '1rem', margin: 0, color: '#666' }}><strong>Email:</strong> {currentUser.email}</p>
      </div>
    </div>
  );
};

const AuthPage = ({ mode, setCurrentUser }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState(""); 
  const [birthday, setBirthday] = useState(""); 
  const navigate = useNavigate();

  const handleAuth = async (e) => {
    e.preventDefault();
    try {
      if (mode === 'signup') {
        const newUser = await createUser({
          username, email, passwordHash: password, birthday, demographics: {} 
        });
        const userData = newUser.user || newUser; 
        setCurrentUser({ ...userData, isAnonymous: false }); 
        navigate('/');
      } else {
        const existingUser = await getUserByUsername(username);
        const userData = existingUser.user || existingUser;
        setCurrentUser({ ...userData, isAnonymous: false });
        navigate('/');
      }
    } catch (err) {
      alert(mode === 'signup' ? `Error creating account: ${err.message}` : `Login failed: ${err.message}`);
    }
  };

  return (
    <div style={pageWrapperStyle}>
      <h1 style={{ ...funTitleStyle, marginBottom: '10px' }}>Welcome to Statit!</h1>
      <h2 style={{ fontSize: '1.8rem', marginBottom: '40px', color: '#666' }}>{mode === 'login' ? 'Log in to continue' : 'Create an account'}</h2>
      <form onSubmit={handleAuth} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', backgroundColor: '#fff', padding: '40px', borderRadius: '20px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)', marginBottom: '20px' }}>
        <input style={inputStyle} placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} required />
        {mode === 'signup' && (
          <>
            <input style={inputStyle} type="email" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} required />
            <input style={inputStyle} type="date" placeholder="Birthday" value={birthday} onChange={e => setBirthday(e.target.value)} required />
          </>
        )}
        <input style={inputStyle} type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} required />
        <button type="submit" style={{ ...mainButtonStyle, width: '280px', fontSize: '1.2rem', backgroundColor: '#8b5cf6', color: 'white' }}>
          {mode === 'login' ? 'Log in' : 'Create account'}
        </button>
      </form>
      {mode === 'login' ? <p>Don't have an account? <Link to="/signup" style={{ color: '#8b5cf6', fontWeight: 'bold' }}>Create one here</Link></p>
        : <p>Already have an account? <Link to="/login" style={{ color: '#8b5cf6', fontWeight: 'bold' }}>Log in here</Link></p>}
    </div>
  );
};

const CategoryList = ({ title, categories }) => (
  <div style={pageWrapperStyle}>
    <Link to="/" style={backLinkStyle}>← Back to Main</Link>
    <h2 style={{ fontSize: '2rem', marginBottom: '30px' }}>{title}</h2>
    {categories.length === 0 ? (
      <div style={{ fontSize: '1.2rem' }}><p>No categories loaded.</p></div>
    ) : (
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px', maxWidth: '900px' }}>
        {categories.map(cat => {
          const actualCatId = cat.categoryId || cat.category_id || cat.id;
          return (
            <Link key={actualCatId || cat.name} to={`/ranking/${actualCatId}`} style={{ 
              width: '160px', height: '160px', border: '2px solid #8b5cf6', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', 
              textDecoration: 'none', color: 'black', fontWeight: 'bold', borderRadius: '15px', backgroundColor: '#fff', fontSize: '1.1rem', boxShadow: '0 4px 10px rgba(0,0,0,0.05)'
            }}>
              <span>{cat.name}</span>
              <span style={{fontSize: '0.8rem', color: '#666', marginTop: '10px'}}>{cat.units || cat.units_of_measurement}</span>
            </Link>
          );
        })}
      </div>
    )}
  </div>
);

const CreateCategory = ({ currentUser, setCategories }) => {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [better, setBetter] = useState("large");
  const [unit, setUnit] = useState("");
  const navigate = useNavigate();

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!name || !unit) return;
    try {
      const newCat = await createCategory({
        name: name,
        description: description,
        units: unit, 
        tags: ["local"], 
        sort_order: better === 'large', 
        founding_username: currentUser.username 
      });
      setCategories(prev => [...prev, newCat.category || newCat]); 
      navigate('/local');
    } catch (err) {
      alert(`Backend rejected the category:\n\n${err.message}`);
    }
  };

  return (
    <div style={pageWrapperStyle}>
      <Link to="/" style={backLinkStyle}>← Back</Link>
      <h2 style={{ fontSize: '2rem' }}>Design Your Ranking</h2>
      <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '20px', alignItems: 'center', backgroundColor: '#fff', padding: '40px', borderRadius: '20px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)' }}>
        <input style={inputStyle} placeholder="Category Name (e.g. Typing Speed)" value={name} onChange={e => setName(e.target.value)} required />
        <input style={inputStyle} placeholder="Description (optional)" value={description} onChange={e => setDescription(e.target.value)} />
        <input style={inputStyle} placeholder="Measurement Unit (e.g. WPM)" value={unit} onChange={e => setUnit(e.target.value)} required />
        
        <div style={{ margin: '10px 0' }}>
          <p style={{ fontWeight: 'bold', marginBottom: '10px' }}>Which is better?</p>
          <button type="button" onClick={() => setBetter('large')} style={{ ...mainButtonStyle, width: 'auto', fontSize: '1rem', padding: '10px 15px', backgroundColor: better === 'large' ? '#8b5cf6' : '#fff', color: better === 'large' ? '#fff' : '#000' }}>Large Number</button>
          <button type="button" onClick={() => setBetter('small')} style={{ ...mainButtonStyle, width: 'auto', fontSize: '1rem', padding: '10px 15px', marginLeft: '10px', backgroundColor: better === 'small' ? '#8b5cf6' : '#fff', color: better === 'small' ? '#fff' : '#000' }}>Small Number</button>
        </div>

        <button type="submit" style={{ ...mainButtonStyle, width: '100%', backgroundColor: '#4CAF50', color: 'white', border: 'none' }}>Create Category</button>
      </form>
    </div>
  );
};

const RankingPage = ({ categories, currentUser }) => {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  
  const catInfo = categories.find(c => 
    String(c.categoryId || c.category_id || c.id) === String(categoryId)
  ) || { name: 'Loading...', units: '' };
  
  const [globalScores, setGlobalScores] = useState([]);
  const [val, setVal] = useState("");
  const [gender, setGender] = useState("Non-binary");
  const [region, setRegion] = useState("North America");
  
  const [hasSubmitted, setHasSubmitted] = useState(false);
  const [isAnonForm, setIsAnonForm] = useState(false);
  const [tableView, setTableView] = useState("Global");

  const fetchLeaderboard = async () => {
    if (!categoryId || categoryId === 'undefined') return; 

    try {
      const data = await getTopScores(categoryId);
      const scoresArray = data.scores?.content || data.content || data.scores || [];
      
      const formattedScores = scoresArray.map(s => {
        let parsedTags = {};
        if (typeof s.tags === 'string') {
          try { parsedTags = JSON.parse(s.tags); } catch (e) {}
        } else if (s.tags) {
          parsedTags = s.tags;
        }

        const isAnon = s.anonymous === true || s.isAnonymous === true || s.is_anonymous === true || String(s.anonymous) === 'true';
        const actualUsername = s.username || s.userName || s.user_name || s.user?.username || s.user?.userName || "Unknown";

        return {
          userId: s.userId || s.user_id || s.user?.id || s.user?.userId || s.user?.user_id,
          name: isAnon ? "Anonymous" : actualUsername,
          value: s.score ?? s.score_value ?? s.scoreValue,
          gender: parsedTags.Gender,
          region: parsedTags.Region
        };
      });
      setGlobalScores(formattedScores);

      if (currentUser) {
        const currentUserId = String(currentUser.id || currentUser.userId || currentUser.user_id);
        const alreadySubmitted = formattedScores.some(s => String(s.userId) === currentUserId);
        setHasSubmitted(alreadySubmitted);
      }

    } catch (err) {
      console.error("Failed to load leaderboard");
    }
  };

  useEffect(() => {
    fetchLeaderboard();
  }, [categoryId, currentUser]);

  const addEntry = async (e) => {
    e.preventDefault();

    if (hasSubmitted) return alert("You have already submitted a score for this category!");

    const userId = currentUser?.id || currentUser?.userId || currentUser?.user_id;

    if (val === "") return alert("Please enter a score.");
    if (!userId) return alert("Error: User ID missing. Please log out and back in.");
    if (!categoryId || categoryId === 'undefined') return alert("Error: Invalid Category ID. Please go back to the Categories page and click the category again.");
    
    const userIsAnon = isAnonForm;

    try {
      await submitScore({
        user_id: userId,
        category_id: categoryId,
        score: parseFloat(val),
        tags: { "Gender": gender, "Region": region },
        anonymous: userIsAnon,
        isAnonymous: userIsAnon,
        is_anonymous: userIsAnon
      });
      setVal(""); 
      setHasSubmitted(true); 
      fetchLeaderboard();
    } catch (err) {
      alert(`Failed to submit score: ${err.message}`);
    }
  };

  const genderScores = globalScores.filter(s => s.gender === gender);
  const regionScores = globalScores.filter(s => s.region === region);
  const intersectionScores = globalScores.filter(s => s.gender === gender && s.region === region);

  const renderTable = (title, statsToRender) => (
    <div key={title} style={{ width: '100%', maxWidth: '400px', maxHeight: '500px', overflowY: 'auto', border: '1px solid #eee', borderRadius: '12px', backgroundColor: '#fff', boxShadow: '0 5px 15px rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column' }}>
      <h3 style={{ margin: '15px 0', fontSize: '1.5rem', color: '#8b5cf6', textAlign: 'center' }}>{title}</h3>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '1.1rem' }}>
        <thead style={{ position: 'sticky', top: 0, backgroundColor: '#8b5cf6', color: 'white', zIndex: 1 }}>
          <tr><th style={{ padding: '12px' }}>Rank</th><th>{catInfo.units || catInfo.units_of_measurement || 'Score'}</th><th>Name</th></tr>
        </thead>
        <tbody>
          {statsToRender.length === 0 ? (
            <tr><td colSpan="3" style={{ padding: '20px', color: '#666' }}>No entries yet.</td></tr>
          ) : (
            statsToRender.map((stat, i) => {
              const actualRank = statsToRender.findIndex(s => s.value === stat.value) + 1;
              
              let rankDisplay = actualRank;
              if (actualRank === 1) rankDisplay = '🥇';
              else if (actualRank === 2) rankDisplay = '🥈';
              else if (actualRank === 3) rankDisplay = '🥉';

              return (
                <tr key={i} style={{ backgroundColor: actualRank <= 3 ? '#fff9e6' : '#fff', borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '12px', fontWeight: actualRank <= 3 ? 'bold' : 'normal', fontSize: actualRank <= 3 ? '1.5rem' : '1rem' }}>
                    {rankDisplay}
                  </td>
                  <td style={{ fontWeight: actualRank <= 3 ? 'bold' : 'normal' }}>{stat.value ?? "-"}</td>
                  <td style={{ fontWeight: actualRank <= 3 ? 'bold' : 'normal', fontStyle: stat.name === 'Anonymous' ? 'italic' : 'normal', color: stat.name === 'Anonymous' ? '#888' : '#000' }}>
                    {stat.name ?? "-"}
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );

  return (
    <div style={{ ...pageWrapperStyle, justifyContent: 'flex-start', paddingTop: '80px' }}>
      <span onClick={() => navigate(-1)} style={{ ...backLinkStyle, cursor: 'pointer' }}>← Back</span>
      <h2 style={{ textTransform: 'capitalize', fontSize: '2.5rem', marginBottom: '20px' }}>{catInfo.name} Rankings</h2>
      
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '30px', justifyContent: 'center', marginBottom: '40px' }}>
        {currentUser ? (
          <form onSubmit={addEntry} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '15px', backgroundColor: '#fff', padding: '25px', borderRadius: '15px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)' }}>
            <h3 style={{ margin: 0, fontSize: '1.3rem' }}>Submit Your Stat</h3>
            <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
              <input type="number" step="any" value={val} onChange={e => setVal(e.target.value)} placeholder="0" disabled={hasSubmitted} style={{ width: '100px', height: '60px', textAlign: 'center', border: '2px solid #8b5cf6', fontSize: '24px', borderRadius: '10px', backgroundColor: hasSubmitted ? '#f5f5f5' : 'white' }} required />
              <span style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>{catInfo.units || catInfo.units_of_measurement}</span>
            </div>
            <select style={{ ...inputStyle, marginBottom: '0', width: '220px' }} value={gender} onChange={e => setGender(e.target.value)}>
              <option value="Non-binary">Non-binary</option>
              <option value="Male">Male</option>
              <option value="Female">Female</option>
            </select>
            <select style={{ ...inputStyle, marginBottom: '0', width: '220px' }} value={region} onChange={e => setRegion(e.target.value)}>
              <option value="North America">North America</option>
              <option value="South America">South America</option>
              <option value="Europe">Europe</option>
              <option value="Asia">Asia</option>
              <option value="Africa">Africa</option>
              <option value="Australia/Oceania">Australia/Oceania</option>
              <option value="Antarctica">Antarctica</option>
            </select>
            
            <label style={{display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '1rem', marginBottom: '5px'}}>
              <input type="checkbox" checked={isAnonForm} onChange={e => setIsAnonForm(e.target.checked)} disabled={hasSubmitted} />
              Submit Anonymously
            </label>

            <button 
              type="submit" 
              disabled={hasSubmitted}
              style={{ 
                ...mainButtonStyle, 
                width: '220px', 
                fontSize: '1.1rem', 
                backgroundColor: hasSubmitted ? '#ccc' : '#8b5cf6', 
                color: 'white',
                cursor: hasSubmitted ? 'not-allowed' : 'pointer'
              }}
            >
              {hasSubmitted ? 'Already Submitted' : 'Add Entry'}
            </button>
          </form>
        ) : (
           <p>Log in to submit a score.</p>
        )}
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%', maxWidth: '1400px' }}>
        <select 
          style={{ padding: '12px', fontSize: '1.1rem', borderRadius: '8px', marginBottom: '20px', border: '2px solid #8b5cf6', outline: 'none', cursor: 'pointer', width: '350px' }} 
          value={tableView} 
          onChange={e => setTableView(e.target.value)}
        >
          <option value="Global">Global Ranking</option>
          <option value="Gender">{gender} Ranking</option>
          <option value="Region">{region} Ranking</option>
          <option value="Intersection">{gender} in {region} Ranking</option>
        </select>

        <div style={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
          {tableView === 'Global' && renderTable('Global Ranking', globalScores)}
          {tableView === 'Gender' && renderTable(`${gender} Ranking`, genderScores)}
          {tableView === 'Region' && renderTable(`${region} Ranking`, regionScores)}
          {tableView === 'Intersection' && renderTable(`${gender} in ${region} Ranking`, intersectionScores)}
        </div>
      </div>
    </div>
  );
};