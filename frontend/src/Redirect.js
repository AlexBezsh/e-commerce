import { useLocation } from "react-router-dom";

const Redirect = () => {

  const location = useLocation();

  window.location.replace(location.state.url);
};

export default Redirect;