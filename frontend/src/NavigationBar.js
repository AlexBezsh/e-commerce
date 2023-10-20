import { Nav, Navbar, NavbarBrand, NavItem, NavLink, Button, NavbarText } from 'reactstrap';
import { useNavigate } from 'react-router-dom';
import { authServerUrl } from './Utils';
import React, { useState } from 'react';
import { jwtDecode } from 'jwt-decode';

function NavigationBar() {

  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loginError, setLoginError] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(localStorage.getItem('isLoggedIn'));

  const handleLogin = () => {
    let requestOptions = {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        'username': email,
        'password': password,
        'client_id': 'e-commerce-app',
        'grant_type': 'password'
      })
    };
    fetch(authServerUrl + '/realms/e-commerce/protocol/openid-connect/token', requestOptions)
      .then(response => {
        if (!response.ok) throw new Error("Invalid credentials");
        return response.json();
      })
      .then(data => saveUser(data))
      .catch(error => {
        console.log(error);
        setLoginError(true);
      });
  };

  const saveUser = (data) => {
    const tokenInfo = jwtDecode(data.access_token);
    setIsLoggedIn(true);
    localStorage.setItem('isLoggedIn', true);
    localStorage.setItem('userName', tokenInfo.name);
    localStorage.setItem('accessToken', data.access_token);
    localStorage.setItem('refreshToken', data.refresh_token);
    window.location.reload(false);
  }

  const handleLogout = () => {
    setIsLoggedIn(false);
    localStorage.clear();
    navigate('/ui/');
    window.location.reload(false);
  };

  return (
    <Navbar expand="lg" className="bg-body-tertiary">
      <NavbarBrand><NavLink href="/">E-Commerce</NavLink></NavbarBrand>
      {isLoggedIn ? (
        <Nav className="justify-content-end" style={{ width: "100%" }} navbar>
          <NavItem><NavLink href="/ui/cart/">Cart</NavLink></NavItem>
          <NavItem><NavLink href="/ui/orders/">Orders</NavLink></NavItem>
          <NavbarText>{localStorage.getItem('userName')}</NavbarText>
          <NavItem style={{ marginLeft: 10 }}>
            <Button variant="outline-success" color="warning" onClick={() => handleLogout()}>Logout</Button>
          </NavItem>
        </Nav>
      ) : (
        <div>
          <Nav className="justify-content-end" style={{ width: "100%" }} navbar>
            {loginError &&
              <NavbarText style={{ marginRight: 10, color: "red" }}>Invalid credentials</NavbarText>
            }
            <input
              type="text"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <NavItem>
              <Button variant="outline-success" color="primary" onClick={() => handleLogin()}>Login</Button>
            </NavItem>
          </Nav>
        </div>
      )}
    </Navbar>
  );
}

export default NavigationBar;
