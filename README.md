# Global Ranking System
Global Ranking System is a platform that allows users to submit their personal metrics (e.g., height, weight, etc.) across various categories and see how they compare to the rest of the world. The system tracks individual user histories while dynamically maintaining global and community baselines, using statistical outlier rejection to ensure data integrity.

## Core Features
### 1. User Accounts & Authentication
- **Account Requirements:** Users must create an account to upload scores or propose new categories. This acts as the primary rate-limiting and anti-spam measure.
- **Verification:** Currently, there is no email or identity verification required for account creation (similar to when2meet). Future scope is to require email registration and potentially other anti-spam measures.
- **Profile Demographics:** Users can save default demographics (Date of Birth, Sex, Region) to their profile to enable some auto-filling for score submissions.

### 2. Category Management
- **Admin Approval:** Users can propose new measurement categories, but all categories require Admin approval before going live to prevent duplicates, maintain appropriateness, and enforce data standards.
- **Granular Configurations:** When creating a category, the following parameters are defined.
    - **Units:** The specific unit of measurement (e.g., cm, lbs, kg).
    - **Separation Rules:** Options for whether the category requires separation by Region, Sex, and/or Age Group.

### 3. Score Tracking & History
- **Personal Records:** The system highlights a user's highest (or best) score in a given category.
- **Score History:** A complete, time-stamped history of all uploads is retained so users can track their progress over time. Only 1 record (the highest value, or less often, the most recent value) is kept in the community table. 

### 4. Global Baselines & Statistics
- **External APIs vs. Internal Logic:** If highly reliable, static data exists (e.g., WHO or CDC LMS parameters), the system will use a seeded table to calculate z-scores.
    - For all other custom categories, the application dynamically calculates community baselines (Mean, Median, Standard Deviation) from aggregated user submissions.
- **Outlier Rejection:** To handle malicious inputs, the system calculates a z-score for every incoming submission. Scores that fall beyond a realistic threshold are saved to the user's history but are flagged as invalid and are excluded from community tables.

### 5. Advanced Analytics (Not yet implemented)
- **Distribution Shape Analaysis:** For internally tracked categories, the system will eventually calculate the shape of the data curve (skewness and kurtosis) to move beyond simple Gaussian distributions and better model real-world data clustering.
- **Dataset Correlation:** Identifying statistical relationships between different categories
