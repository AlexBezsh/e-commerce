import { serverUrl, jwtExpiredMessage, refreshToken } from './Utils';
import { useParams, useNavigate } from "react-router-dom";
import React, { useState, useEffect } from 'react';

const CancelOrder = () => {

  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [redirect, setRedirect] = useState(true);

  useEffect(() => {
    setLoading(true);
    let requestOptions = {
      method: 'PATCH',
      headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
      }
    };
    let is401 = false;
    fetch(serverUrl + '/api/v1/orders/' + id + "/cancel", requestOptions)
      .then(response => {
        if (response.ok) {
          alert("Order is cancelled");
          return null;
        }
        if (response.status === 401) is401 = true;
        return response.json();
      })
      .then(data => {
        if (data !== null) {
          if (is401 && data.message.includes(jwtExpiredMessage)) {
            refreshToken(() => window.location.reload());
            setRedirect(false);
          } else {
            alert("Failed to cancel order. Reason: " + JSON.stringify(data));
          }
        }
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <p>Loading...</p>;
  }

  if (redirect) {
    navigate('/ui/orders/');
  }
  return null;
};

export default CancelOrder;