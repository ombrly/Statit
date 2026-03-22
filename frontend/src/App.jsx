import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useParams, useNavigate, Navigate } from 'react-router-dom';

// --- API HELPERS ---
const API_BASE_URL = '/api/v1';

const createUser = async (userData) => {
  const response = await fetch(`${API_BASE_URL}/users`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData),
  });
  if (!response.ok) throw new Error('Failed to create user');
  return response.json();
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
  if (!response.ok) throw new Error('Failed to create category');
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
  if (!response.ok) throw new Error('Failed to submit score');
  return response.json();
};

const getTopScores = async (categoryId, page = 0, size = 25) => {
  const response = await fetch(`${API_BASE_URL}/leaderboards/${categoryId}/top?page=${page}&size=${size}`);
  if (!response.ok) throw new Error('Failed to fetch leaderboard');
  return response.json();
};

// --- STYLES & LOCAL STORAGE HELPER ---
const getStorage = (key, defaultValue) => {
  const saved = localStorage.getItem(key);
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

// --- MAIN APP ---
export default function App() {
  const [categories, setCategories] = useState([]);
  const [currentUser, setCurrentUser] = useState(() => getStorage('currentUser', null));

  // Keep current user logged in across refreshes
  useEffect(() => {
    localStorage.setItem('currentUser', JSON.stringify(currentUser));
  }, [currentUser]);

  // Fetch all categories on initial load
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const data = await getAllCategories();
        // Assuming your paginated response has the list in data.categories or data.content
        const categoryList = data.categories || data.content || data; 
        setCategories(categoryList);
      } catch (err) {
        console.error("Failed to load categories:", err);
      }
    };
    fetchCategories();
  }, []);

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

// --- APP CONTENT ---
const AppContent = ({ categories, setCategories, currentUser, setCurrentUser }) => {
  const logout = () => setCurrentUser(null);

  return (
    <>
      <div style={topNavStyle}>
        <Link to="/">
          <img src="/logo.webp" alt="Statit Logo" style={logoStyle} />
        </Link>
        <div style={navLinksStyle}>
          <Link to="/global"><button className="nav-button">Global Categories</button></Link>
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
        
        <Route path="/global" element={<CategoryList title="Global Categories" categories={categories} />} />
        <Route path="/create" element={currentUser ? <CreateCategory currentUser={currentUser} setCategories={setCategories} /> : <Navigate to="/login" />} />
        <Route path="/ranking/:categoryId" element={<RankingPage categories={categories} currentUser={currentUser} />} />
      </Routes>
    </>
  );
};

// --- HOME PAGE ---
const Home = () => (
  <div style={pageWrapperStyle}>
    <h1 style={funTitleStyle}>Welcome to Statit</h1>
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', marginBottom: '40px' }}>
      <Link to="/global"><button style={mainButtonStyle}>Global Categories</button></Link>
      <Link to="/create"><button style={mainButtonStyle}>Create Your Own</button></Link>
    </div>
  </div>
);

// --- PROFILE PAGE ---
const ProfilePage = ({ currentUser, setCurrentUser }) => {
  const toggleAnonymous = () => {
    setCurrentUser({ ...currentUser, isAnonymous: !currentUser.isAnonymous });
  };

  return (
    <div style={pageWrapperStyle}>
      <Link to="/" style={backLinkStyle}>← Back to Main</Link>
      <h2 style={{ fontSize: '2.5rem', marginBottom: '30px' }}>Your Profile</h2>
      <div style={{ backgroundColor: '#fff', padding: '40px', borderRadius: '20px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '20px', minWidth: '350px' }}>
        <p style={{ fontSize: '1.4rem', margin: 0 }}><strong>Username:</strong> {currentUser.username}</p>
        <p style={{ fontSize: '1rem', margin: 0, color: '#666' }}><strong>Email:</strong> {currentUser.email}</p>
        <div style={{ borderTop: '1px solid #eee', width: '100%', margin: '10px 0' }}></div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <span style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>Anonymous Mode:</span>
          <button onClick={toggleAnonymous} style={{ ...smallButtonStyle, backgroundColor: currentUser.isAnonymous ? '#8b5cf6' : '#fff', color: currentUser.isAnonymous ? '#fff' : '#333', width: '80px' }}>
            {currentUser.isAnonymous ? 'ON' : 'OFF'}
          </button>
        </div>
      </div>
    </div>
  );
};

// --- LOGIN / SIGNUP PAGE ---
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
        setCurrentUser({ ...newUser.user, isAnonymous: false }); 
        navigate('/');
      } else {
        const existingUser = await getUserByUsername(username);
        setCurrentUser({ ...existingUser.user, isAnonymous: false });
        navigate('/');
      }
    } catch (err) {
      alert(mode === 'signup' ? "Error creating account. Username might be taken." : "User not found. Please try again.");
    }
  };

  return (
    <div style={pageWrapperStyle}>
      <h1 style={{ ...funTitleStyle, marginBottom: '10px' }}>Welcome to Statit</h1>
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

// --- CATEGORY LIST PAGE ---
const CategoryList = ({ title, categories }) => (
  <div style={pageWrapperStyle}>
    <Link to="/" style={backLinkStyle}>← Back to Main</Link>
    <h2 style={{ fontSize: '2rem', marginBottom: '30px' }}>{title}</h2>
    {categories.length === 0 ? (
      <div style={{ fontSize: '1.2rem' }}><p>No categories loaded.</p></div>
    ) : (
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px', maxWidth: '900px' }}>
        {categories.map(cat => (
          <Link key={cat.id || cat.name} to={`/ranking/${cat.id}`} style={{ 
            width: '160px', height: '160px', border: '2px solid #8b5cf6', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', 
            textDecoration: 'none', color: 'black', fontWeight: 'bold', borderRadius: '15px', backgroundColor: '#fff', fontSize: '1.1rem', boxShadow: '0 4px 10px rgba(0,0,0,0.05)'
          }}>
            <span>{cat.name}</span>
            <span style={{fontSize: '0.8rem', color: '#666', marginTop: '10px'}}>{cat.units}</span>
          </Link>
        ))}
      </div>
    )}
  </div>
);

// --- CREATE CATEGORY PAGE ---
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
        name, description, units: unit, tags: [], 
        sort_order: better === 'large', founding_username: currentUser.username
      });
      setCategories(prev => [...prev, newCat.category || newCat]); 
      navigate('/global');
    } catch (err) {
      alert("Failed to create category. Ensure the backend is running.");
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
const RankingPage = ({ categories, currentUser }) => {
  const { categoryId } = useParams();
  const catInfo = categories.find(c => c.id === categoryId) || { name: 'Loading...', units: '' };
  
  const [globalScores, setGlobalScores] = useState([]);
  const [val, setVal] = useState("");
  const [gender, setGender] = useState("Male");
  const [region, setRegion] = useState("North America");

  const fetchLeaderboard = async () => {
    try {
      const data = await getTopScores(categoryId);
      const scoresArray = data.scores?.content || data.content || data.scores || [];
      const formattedScores = scoresArray.map(s => ({
        name: s.anonymous ? "Anonymous" : (s.user?.username || "Unknown"),
        value: s.score
      }));
      setGlobalScores(formattedScores);
    } catch (err) {
      console.error("Failed to load leaderboard");
    }
  };

  useEffect(() => {
    if (categoryId) fetchLeaderboard();
  }, [categoryId]);

  const addEntry = async (e) => {
    e.preventDefault();
    if (!val || !currentUser?.id) return alert("Error: User ID missing. Please log out and back in.");
    
    try {
      await submitScore({
        user_id: currentUser.id,
        category_id: categoryId,
        score: parseFloat(val),
        tags: { "Gender": gender, "Region": region },
        anonymous: currentUser.isAnonymous || false
      });
      setVal(""); 
      fetchLeaderboard();
    } catch (err) {
      alert("Failed to submit score.");
    }
  };

  const renderTable = (title, statsToRender) => (
    <div key={title} style={{ width: '100%', maxWidth: '400px', maxHeight: '500px', overflowY: 'auto', border: '1px solid #eee', borderRadius: '12px', backgroundColor: '#fff', boxShadow: '0 5px 15px rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column' }}>
      <h3 style={{ margin: '15px 0', fontSize: '1.4rem', color: '#8b5cf6' }}>{title} Table</h3>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '1.1rem' }}>
        <thead style={{ position: 'sticky', top: 0, backgroundColor: '#8b5cf6', color: 'white', zIndex: 1 }}>
          <tr><th style={{ padding: '12px' }}>Rank</th><th>{catInfo.units || 'Score'}</th><th>Name</th></tr>
        </thead>
        <tbody>
          {statsToRender.length === 0 ? (
            <tr><td colSpan="3" style={{ padding: '20px', color: '#666' }}>No entries yet.</td></tr>
          ) : (
            statsToRender.map((stat, i) => (
              <tr key={i} style={{ backgroundColor: i < 3 ? '#fff9e6' : '#fff', borderBottom: '1px solid #eee' }}>
                <td style={{ padding: '12px', fontWeight: i < 3 ? 'bold' : 'normal' }}>{i + 1}</td>
                <td>{stat.value ?? "-"}</td>
                <td style={{ fontWeight: i < 3 ? 'bold' : 'normal', fontStyle: stat.name === 'Anonymous' ? 'italic' : 'normal' }}>{stat.name ?? "-"}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );

  return (
    <div style={{ ...pageWrapperStyle, justifyContent: 'flex-start', paddingTop: '80px' }}>
      <Link to="/global" style={backLinkStyle}>← Back</Link>
      <h2 style={{ textTransform: 'capitalize', fontSize: '2.5rem', marginBottom: '20px' }}>{catInfo.name} Rankings</h2>
      
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '30px', justifyContent: 'center', marginBottom: '40px' }}>
        {currentUser ? (
          <form onSubmit={addEntry} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '15px', backgroundColor: '#fff', padding: '25px', borderRadius: '15px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)' }}>
            <h3 style={{ margin: 0, fontSize: '1.3rem' }}>Submit Your Stat</h3>
            <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
              <input type="number" step="any" value={val} onChange={e => setVal(e.target.value)} placeholder="0" style={{ width: '100px', height: '60px', textAlign: 'center', border: '2px solid #8b5cf6', fontSize: '24px', borderRadius: '10px' }} required />
              <span style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>{catInfo.units}</span>
            </div>
            <select style={{ ...inputStyle, marginBottom: '0', width: '220px' }} value={gender} onChange={e => setGender(e.target.value)}>
              <option value="Male">Male</option><option value="Female">Female</option>
            </select>
            <select style={{ ...inputStyle, marginBottom: '0', width: '220px' }} value={region} onChange={e => setRegion(e.target.value)}>
              <option value="North America">North America</option><option value="Europe">Europe</option><option value="Asia">Asia</option>
            </select>
            <button type="submit" style={{ ...mainButtonStyle, width: '220px', fontSize: '1.1rem', backgroundColor: '#8b5cf6', color: 'white' }}>Add Entry</button>
          </form>
        ) : (
           <p>Log in to submit a score.</p>
        )}
      </div>

      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '25px', width: '100%', maxWidth: '1400px' }}>
        {renderTable('Global Top', globalScores)}
      </div>
    </div>
  );
};