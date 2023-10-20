export const jwtExpiredMessage = 'Jwt expired';
export const serverUrl = 'http://localhost:8080'
export const authServerUrl = 'http://localhost:8082'

export const refreshToken = (refreshCallback) => {
  let requestOptions = {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      'refresh_token': localStorage.getItem('refreshToken'),
      'client_id': 'e-commerce-app',
      'grant_type': 'refresh_token'
    })
  };
  fetch(authServerUrl + '/realms/e-commerce/protocol/openid-connect/token', requestOptions)
    .then(response => {
      if (!response.ok) throw new Error();
      return response.json();
    })
    .then(data => {
      localStorage.setItem('accessToken', data.access_token);
      localStorage.setItem('refreshToken', data.refresh_token);
      refreshCallback();
    })
    .catch(error => {
      alert("Failed to refresh token");
      localStorage.clear();
      window.location.replace('/ui');
    });
};