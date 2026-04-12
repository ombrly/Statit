// Compile and run steps (locally):
// In backend:
// docker rm -f backend-db-1 db
// docker compose up -d
// if nothing works, then sudo systemctl stop postgresql --> docker-compose up -d --build
// Frontend: 
// npm install
// npm run dev

//cd ~/Global-Ranking-System/backend
//docker-compose up -d --build 
//Above runs dbeaver

/*
Below is to get azure connected to the neon database. Only needs to be done once:
az containerapp update \
  --name statit-backend \
  --resource-group STATITgroup \
  --set-env-vars SPRING_DATASOURCE_URL="jdbc:postgresql://ep-wandering-cherry-a8balq59.eastus2.azure.neon.tech/neondb?sslmode=require" SPRING_DATASOURCE_USERNAME="neondb_owner" SPRING_DATASOURCE_PASSWORD="npg_rqW1pGoDZ4JR"


For frontend, if new code is added, go to frontend directory and run:

cd ~/Global-Ranking-System/frontend
docker build --no-cache -t ca93d2246cc8acr.azurecr.io/statit-frontend:v7 .
az acr login --name ca93d2246cc8acr
docker push ca93d2246cc8acr.azurecr.io/statit-frontend:v7
az containerapp update --name statit-frontend --resource-group STATITgroup --image ca93d2246cc8acr.azurecr.io/statit-frontend:v7

(Just increase the version number each time you do this to avoid caching issues)

Run on backend: 

# Turn on (order doesn't matter anymore since DB is in Neon)
az containerapp update --name statit-backend --resource-group STATITgroup --min-replicas 1
az containerapp update --name statit-frontend --resource-group STATITgroup --min-replicas 1
az containerapp update --name statit-db --resource-group STATITgroup --min-replicas 1

Turn azure off
az containerapp update --name statit-frontend --resource-group STATITgroup --min-replicas 0
az containerapp update --name statit-backend --resource-group STATITgroup --min-replicas 0
az containerapp update --name statit-db --resource-group STATITgroup --min-replicas 0

https://statit-frontend.bluemeadow-174af2a3.eastus.azurecontainerapps.io

*/


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
  if (!response.ok) {
    const errText = await response.text();
    throw new Error(errText);
  }
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

// --- STYLES ---
const pageWrapperStyle = { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', width: '100vw', fontFamily: 'sans-serif', padding: '20px', paddingTop: '100px', boxSizing: 'border-box', textAlign: 'center', position: 'relative', backgroundColor: '#fdfdfd' };
const topNavStyle = { position: 'fixed', top: 0, left: 0, width: '100vw', height: '75px', backgroundColor: '#fff', boxShadow: '0 2px 10px rgba(0,0,0,0.1)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 30px', boxSizing: 'border-box', zIndex: 1000 };
const logoStyle = { height: '50px', width: 'auto', cursor: 'pointer' };
const navLinksStyle = { display: 'flex', gap: '15px', alignItems: 'center' };
const backLinkStyle = { position: 'absolute', top: '100px', left: '20px', textDecoration: 'none', color: '#8b5cf6', fontWeight: 'bold', fontSize: '1.1rem', zIndex: 10 };
const inputStyle = { padding: '12px', borderRadius: '8px', border: '1px solid #ccc', width: '280px', marginBottom: '15px', fontSize: '1rem' };
const mainButtonStyle = { padding: '15px 20px', cursor: 'pointer', borderRadius: '8px', border: '2px solid #8b5cf6', backgroundColor: '#fff', fontSize: '1.4rem', width: '380px', fontWeight: 'bold', transition: '0.2s', color: '#333' };
const smallButtonStyle = { padding: '10px 18px', cursor: 'pointer', borderRadius: '6px', border: '2px solid #8b5cf6', backgroundColor: '#fff', fontSize: '1rem', fontWeight: 'bold', color: '#333', transition: '0.2s' };
const funTitleStyle = { fontSize: '3.2rem', marginBottom: '40px', color: '#2c3e50', fontFamily: '"Comic Sans MS", "Chalkboard SE", "Marker Felt", cursive' };

// Custom styles for our checkbox dropdowns
const dropdownButtonStyle = { ...inputStyle, width: '220px', marginBottom: 0, display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', backgroundColor: '#fff', textAlign: 'left', userSelect: 'none' };
const dropdownMenuStyle = { position: 'absolute', top: '100%', left: 0, width: '100%', backgroundColor: '#fff', border: '1px solid #ccc', borderRadius: '8px', padding: '10px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)', zIndex: 100, display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '5px', boxSizing: 'border-box', textAlign: 'left' };
const checkboxLabelStyle = { display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer', fontSize: '0.95rem', userSelect: 'none' };

// --- CONSTANTS ---
const ALL_GENDERS = ["Non-binary", "Male", "Female"];
const ALL_REGIONS = ["North America", "South America", "Europe", "Asia", "Africa", "Australia/Oceania", "Antarctica"];

export default function App() {
  const [categories, setCategories] = useState([]);
  const [currentUser, setCurrentUser] = useState(() => getStorage('currentUser', null));

  useEffect(() => {
    sessionStorage.setItem('currentUser', JSON.stringify(currentUser));
  }, [currentUser]);

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
        
        {/* NEW ROUTING STRUCTURE */}
        <Route path="/ranking/:categoryId" element={<RankingPage categories={categories} currentUser={currentUser} />} />
        <Route path="/submit/:categoryId" element={currentUser ? <SubmitStatPage categories={categories} currentUser={currentUser} /> : <Navigate to="/login" />} />
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

// --- DEDICATED SUBMISSION PAGE ---
const SubmitStatPage = ({ categories, currentUser }) => {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  
  const catInfo = categories.find(c => String(c.categoryId || c.category_id || c.id) === String(categoryId)) || { name: 'Loading...', units: '' };
  
  const [val, setVal] = useState("");
  const [gender, setGender] = useState("Non-binary");
  const [region, setRegion] = useState("North America");
  const [isAnonForm, setIsAnonForm] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmission = async (e) => {
    e.preventDefault();
    const userId = currentUser?.id || currentUser?.userId || currentUser?.user_id;

    if (val === "") return alert("Please enter a score.");
    if (!userId) return alert("Error: User ID missing. Please log out and back in.");
    
    setIsSubmitting(true);

    try {
      await submitScore({
        user_id: userId,
        category_id: categoryId,
        score: parseFloat(val),
        tags: { "Gender": gender, "Region": region },
        anonymous: isAnonForm,
        isAnonymous: isAnonForm,
        is_anonymous: isAnonForm
      });
      // Redirect directly to the leaderboard page upon success
      navigate(`/ranking/${categoryId}`);
    } catch (err) {
      alert(`Failed to submit score: ${err.message}`);
      setIsSubmitting(false);
    }
  };

  return (
    <div style={pageWrapperStyle}>
      <span onClick={() => navigate(-1)} style={{ ...backLinkStyle, cursor: 'pointer' }}>← Back to Rankings</span>
      <h2 style={{ textTransform: 'capitalize', fontSize: '2.5rem', marginBottom: '20px' }}>Submit to {catInfo.name}</h2>
      
      <form onSubmit={handleSubmission} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '15px', backgroundColor: '#fff', padding: '40px', borderRadius: '15px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)' }}>
        <h3 style={{ margin: 0, fontSize: '1.3rem' }}>Enter Your Stat</h3>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <input type="number" step="any" value={val} onChange={e => setVal(e.target.value)} placeholder="0" disabled={isSubmitting} style={{ width: '120px', height: '60px', textAlign: 'center', border: '2px solid #8b5cf6', fontSize: '24px', borderRadius: '10px', backgroundColor: isSubmitting ? '#f5f5f5' : 'white' }} required />
          <span style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>{catInfo.units || catInfo.units_of_measurement}</span>
        </div>
        
        <select style={{ ...inputStyle, marginBottom: '0', width: '250px' }} value={gender} onChange={e => setGender(e.target.value)}>
          <option value="Non-binary">Non-binary</option>
          <option value="Male">Male</option>
          <option value="Female">Female</option>
        </select>
        
        <select style={{ ...inputStyle, marginBottom: '0', width: '250px' }} value={region} onChange={e => setRegion(e.target.value)}>
          <option value="North America">North America</option>
          <option value="South America">South America</option>
          <option value="Europe">Europe</option>
          <option value="Asia">Asia</option>
          <option value="Africa">Africa</option>
          <option value="Australia/Oceania">Australia/Oceania</option>
          <option value="Antarctica">Antarctica</option>
        </select>
        
        <label style={{display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '1rem', marginTop: '10px'}}>
          <input type="checkbox" checked={isAnonForm} onChange={e => setIsAnonForm(e.target.checked)} disabled={isSubmitting} />
          Submit Anonymously
        </label>

        <button 
          type="submit" 
          disabled={isSubmitting}
          style={{ 
            ...mainButtonStyle, 
            width: '250px', 
            fontSize: '1.1rem', 
            backgroundColor: isSubmitting ? '#ccc' : '#8b5cf6', 
            color: 'white',
            cursor: isSubmitting ? 'not-allowed' : 'pointer',
            marginTop: '10px'
          }}
        >
          {isSubmitting ? 'Submitting...' : 'Submit Score'}
        </button>
      </form>
    </div>
  );
};


// --- PAGE X: THE LEADERBOARD PAGE ---
const RankingPage = ({ categories, currentUser }) => {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  
  const catInfo = categories.find(c => 
    String(c.categoryId || c.category_id || c.id) === String(categoryId)
  ) || { name: 'Loading...', units: '' };
  
  const [globalScores, setGlobalScores] = useState([]);
  
  // Custom multi-select state
  const [showGlobal, setShowGlobal] = useState(true);
  const [selectedGenders, setSelectedGenders] = useState([]);
  const [selectedRegions, setSelectedRegions] = useState([]);
  
  // To handle which dropdown is currently open (null, 'global', 'gender', 'region')
  const [openDropdown, setOpenDropdown] = useState(null);

  const [hasSubmitted, setHasSubmitted] = useState(false);

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

  const toggleGender = (gender) => {
    setSelectedGenders(prev => 
      prev.includes(gender) ? prev.filter(g => g !== gender) : [...prev, gender]
    );
  };

  const toggleRegion = (region) => {
    setSelectedRegions(prev => 
      prev.includes(region) ? prev.filter(r => r !== region) : [...prev, region]
    );
  };

  const renderTable = (title, statsToRender, keyIndex) => (
    <div key={`${title}-${keyIndex}`} style={{ width: '100%', minWidth: '350px', maxWidth: '450px', maxHeight: '500px', overflowY: 'auto', border: '1px solid #eee', borderRadius: '12px', backgroundColor: '#fff', boxShadow: '0 5px 15px rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', flex: '1 1 350px' }}>
      <h3 style={{ margin: '15px 0', fontSize: '1.3rem', color: '#8b5cf6', textAlign: 'center' }}>{title}</h3>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '1.1rem' }}>
        <thead style={{ position: 'sticky', top: 0, backgroundColor: '#8b5cf6', color: 'white', zIndex: 1 }}>
          <tr><th style={{ padding: '12px' }}>Rank</th><th>{catInfo.units || catInfo.units_of_measurement || 'Score'}</th><th>Name</th></tr>
        </thead>
        <tbody>
          {statsToRender.length === 0 ? (
            <tr><td colSpan="3" style={{ padding: '20px', color: '#666', textAlign: 'center' }}>No entries yet.</td></tr>
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

  // Dynamic logic for generating tables based on multi-select checkboxes
  const generateTablesToDisplay = () => {
    const tables = [];
    
    // 1. Show Global Table if checked
    if (showGlobal) {
      tables.push(renderTable('Global Ranking', globalScores, 'global'));
    }

    // 2. Determine if we are showing broad gender/region tables OR specific intersection tables
    if (selectedGenders.length > 0 && selectedRegions.length === 0) {
      // Only genders selected: show broad gender tables
      selectedGenders.forEach((g, idx) => {
        tables.push(renderTable(`${g} Ranking`, globalScores.filter(s => s.gender === g), `gender-${idx}`));
      });
    } else if (selectedRegions.length > 0 && selectedGenders.length === 0) {
      // Only regions selected: show broad region tables
      selectedRegions.forEach((r, idx) => {
        tables.push(renderTable(`${r} Ranking`, globalScores.filter(s => s.region === r), `region-${idx}`));
      });
    } else if (selectedGenders.length > 0 && selectedRegions.length > 0) {
      // BOTH genders and regions selected: only show the specific intersection tables
      selectedGenders.forEach((g, gIdx) => {
        selectedRegions.forEach((r, rIdx) => {
          tables.push(
            renderTable(`${g} in ${r}`, globalScores.filter(s => s.gender === g && s.region === r), `intersection-${gIdx}-${rIdx}`)
          );
        });
      });
    }

    if (tables.length === 0) {
      return <p style={{ color: '#999', fontStyle: 'italic', fontSize: '1.2rem' }}>Please select at least one leaderboard filter to display.</p>;
    }

    return tables;
  };

  return (
    <div style={{ ...pageWrapperStyle, justifyContent: 'flex-start', paddingTop: '80px' }}>
      <span onClick={() => navigate(-1)} style={{ ...backLinkStyle, cursor: 'pointer' }}>← Back</span>
      <h2 style={{ textTransform: 'capitalize', fontSize: '2.5rem', marginBottom: '20px' }}>{catInfo.name} Rankings</h2>
      
      {/* Invisible overlay to close dropdowns when clicking outside */}
      {openDropdown && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', zIndex: 99 }} onClick={() => setOpenDropdown(null)} />
      )}

      {/* Dynamic Action Button / Text */}
      <div style={{ marginBottom: '30px', zIndex: 1 }}>
        {currentUser ? (
          <button 
            onClick={() => !hasSubmitted && navigate(`/submit/${categoryId}`)} 
            disabled={hasSubmitted}
            style={{ 
              ...mainButtonStyle, 
              width: '300px', 
              backgroundColor: hasSubmitted ? '#ccc' : '#8b5cf6', 
              color: 'white', 
              cursor: hasSubmitted ? 'not-allowed' : 'pointer' 
            }}
          >
            {hasSubmitted ? 'Already Submitted' : 'Submit Your Score'}
          </button>
        ) : (
          <p style={{ fontSize: '1.2rem', color: '#666', fontStyle: 'italic' }}>Log in to submit a score.</p>
        )}
      </div>

      {/* Leaderboard Filters */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%', maxWidth: '1400px', zIndex: 100 }}>
        
        {/* Filter Controls Panel */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', alignItems: 'center', marginBottom: '30px', backgroundColor: '#fff', padding: '25px', borderRadius: '12px', boxShadow: '0 4px 10px rgba(0,0,0,0.05)', width: '100%', maxWidth: '900px' }}>
          
          <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', justifyContent: 'center' }}>
            
            {/* Global Options Dropdown */}
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', position: 'relative' }}>
              <label style={{ fontSize: '0.9rem', fontWeight: 'bold', color: '#666', marginBottom: '5px' }}>Global Settings:</label>
              <div 
                style={dropdownButtonStyle} 
                onClick={() => setOpenDropdown(openDropdown === 'global' ? null : 'global')}
              >
                <span>Global Options</span>
                <span style={{ fontSize: '0.8rem' }}>▼</span>
              </div>
              
              {openDropdown === 'global' && (
                <div style={dropdownMenuStyle}>
                  <label style={checkboxLabelStyle}>
                    <input type="checkbox" checked={showGlobal} onChange={e => setShowGlobal(e.target.checked)} />
                    Show Global Table
                  </label>
                </div>
              )}
            </div>

            {/* Gender Filter Dropdown */}
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', position: 'relative' }}>
              <label style={{ fontSize: '0.9rem', fontWeight: 'bold', color: '#666', marginBottom: '5px' }}>Filter by Gender:</label>
              <div 
                style={dropdownButtonStyle} 
                onClick={() => setOpenDropdown(openDropdown === 'gender' ? null : 'gender')}
              >
                <span>{selectedGenders.length === 0 ? "Select Gender..." : `Selected (${selectedGenders.length})`}</span>
                <span style={{ fontSize: '0.8rem' }}>▼</span>
              </div>

              {openDropdown === 'gender' && (
                <div style={dropdownMenuStyle}>
                  {ALL_GENDERS.map(g => (
                    <label key={g} style={checkboxLabelStyle}>
                      <input type="checkbox" checked={selectedGenders.includes(g)} onChange={() => toggleGender(g)} />
                      {g}
                    </label>
                  ))}
                </div>
              )}
            </div>

            {/* Region Filter Dropdown */}
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', position: 'relative' }}>
              <label style={{ fontSize: '0.9rem', fontWeight: 'bold', color: '#666', marginBottom: '5px' }}>Filter by Region:</label>
              <div 
                style={dropdownButtonStyle} 
                onClick={() => setOpenDropdown(openDropdown === 'region' ? null : 'region')}
              >
                <span>{selectedRegions.length === 0 ? "Select Region..." : `Selected (${selectedRegions.length})`}</span>
                <span style={{ fontSize: '0.8rem' }}>▼</span>
              </div>

              {openDropdown === 'region' && (
                <div style={dropdownMenuStyle}>
                  {ALL_REGIONS.map(r => (
                    <label key={r} style={checkboxLabelStyle}>
                      <input type="checkbox" checked={selectedRegions.includes(r)} onChange={() => toggleRegion(r)} />
                      {r}
                    </label>
                  ))}
                </div>
              )}
            </div>

          </div>
        </div>

        {/* Dynamic Tables Container */}
        <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px', width: '100%', zIndex: 1 }}>
          {generateTablesToDisplay()}
        </div>
      </div>
    </div>
  );
};