import { BellIcon, ChatIcon, FriendRequestIcon, HomeIcon } from "./icons";

export function MobileBottomNav() {
  return (
    <div className="_mobile_navigation_bottom_wrapper">
      <ul className="_mobile_navigation_bottom_list">
        <li className="_mobile_navigation_bottom_link _mobile_navigation_bottom_link_active">
          <a href="#0">
            <HomeIcon />
          </a>
        </li>
        <li className="_mobile_navigation_bottom_link">
          <a href="#0">
            <FriendRequestIcon />
          </a>
        </li>
        <li className="_mobile_navigation_bottom_link">
          <span>
            <BellIcon />
            <span className="_counting">3</span>
          </span>
        </li>
        <li className="_mobile_navigation_bottom_link">
          <a href="#0">
            <ChatIcon />
            <span className="_counting">2</span>
          </a>
        </li>
      </ul>
    </div>
  );
}
