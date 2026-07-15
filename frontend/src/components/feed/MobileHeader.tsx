import { useState } from "react";
import type { CurrentUser } from "../../api/types";
import { BellIcon, ChatIcon, CloseIcon, FriendRequestIcon, HomeIcon, MenuIcon, SearchIcon } from "./icons";

interface MobileHeaderProps {
  user: CurrentUser | undefined;
  onLogout: () => void;
}

export function MobileHeader({ user, onLogout }: MobileHeaderProps) {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="_header_mobile_menu">
      <div className="_header_mobile_menu_top_inner">
        <div className="_logo_wrap">
          <img src="/assets/images/logo.svg" alt="News Feed" className="_nav_logo" />
        </div>

        <div className="_header_mobile_menu_right">
          <span className="_header_mobile_search">
            <SearchIcon />
          </span>
          <button type="button" className="_header_mobile_btn_link" onClick={() => setMenuOpen((v) => !v)}>
            {menuOpen ? <CloseIcon /> : <MenuIcon />}
          </button>
        </div>
      </div>

      {menuOpen && (
        <div style={{ display: "flex", flexDirection: "column", gap: 12, padding: "0 0 16px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <a className="nav-link _header_nav_link_active _header_nav_link" href="#0">
              <HomeIcon />
            </a>
            <a className="nav-link _header_nav_link" href="#0">
              <FriendRequestIcon />
            </a>
            <span className="nav-link _header_nav_link">
              <BellIcon />
            </span>
            <a className="nav-link _header_nav_link" href="#0">
              <ChatIcon />
            </a>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <img src={user?.avatarUrl ?? "/assets/images/profile.png"} alt="" className="_nav_profile_img" style={{ width: 32, height: 32, borderRadius: "50%" }} />
            <span className="_header_nav_para">{user ? `${user.firstName} ${user.lastName}` : ""}</span>
            <button type="button" onClick={onLogout} style={{ marginLeft: "auto", background: "none", border: "none" }}>
              Log Out
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
