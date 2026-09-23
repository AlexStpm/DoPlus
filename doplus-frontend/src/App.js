import './App.css';
import './bootstrapButtons.css'
import { Routes, Route, Navigate } from 'react-router-dom';

import LogIn from './components/LogIn';
import Layout from './components/Layout';
import Missing from './components/Missing';
import Unauthorized from './components/Unauthorized';
import AdminPage from './components/AdminPage';
import RequireAuth from './components/RequireAuth';
import ViewBoard from './components/ViewBoard';
import ViewTime from './components/ViewTime';
import ViewTask from './components/ViewTask';
import MangeTime from './components/MangeTime';
import ManageBoard from './components/ManageBoard';

function App() {
  return (
    <Routes>
      <Route path='/' element={<Layout />}>

        <Route path='login' element={<LogIn />} />

        <Route path='/' element={<Navigate to="login" />} />

        <Route element={<RequireAuth allowedUser="rootadmin" />}>
          <Route path='admin' element={<AdminPage />} />
        </Route>

        <Route element={<RequireAuth />}>
          <Route path='board' element={<ViewBoard />} />
          <Route path='board/:boardId' element={<ViewBoard />} />
          <Route path='createBoard' element={<ManageBoard />} />
          <Route path='editBoard/:boardId' element={<ManageBoard />} />
          <Route path='timeManagement/:userId' element={<ViewTime />} />
          <Route path='timeManagement/edit/:timeId' element={<MangeTime />} />
          <Route path='timeManagement/new' element={<MangeTime />} />
          <Route path='board/:boardId/task/:taskId' element={<ViewTask />} />
        </Route>

        <Route path='unauthorized' element={<Unauthorized />} />
        <Route path='*' element={<Missing />} />
      </Route>

    </Routes>
  );
}

export default App;
