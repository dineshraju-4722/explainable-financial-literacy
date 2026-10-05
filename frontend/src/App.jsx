import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import AssessmentTakePage from './pages/AssessmentTakePage';
import AssessmentResultPage from './pages/AssessmentResultPage';
import EducationContentPage from './pages/EducationContentPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/take" element={<AssessmentTakePage />} />
        <Route path="/assessments/:assessmentId/result" element={<AssessmentResultPage />} />
        <Route path="/education/content/:contentId" element={<EducationContentPage />} />
      </Routes>
    </BrowserRouter>
  );
}
