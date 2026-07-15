import { useState } from "react";
import type { CurrentUser } from "../../api/types";
import { BellIcon, CaretDownIcon, ChatIcon, FriendRequestIcon, HomeIcon, KebabIcon, SearchIcon } from "./icons";

const NOTIFICATIONS = [
  { name: "Steve Jobs", image: "/assets/images/friend-req.png", text: "posted a link in your timeline.", time: "42 minutes ago" },
  { name: "An admin", image: "/assets/images/profile-1.png", text: 'changed the name of the group "Freelancer USA".', time: "1 hour ago" },
  { name: "Ryan Roslansky", image: "/assets/images/friend-req.png", text: "commented on your photo.", time: "3 hours ago" },
];

interface HeaderNavProps {
  user: CurrentUser | undefined;
  onLogout: () => void;
}

export function HeaderNav({ user, onLogout }: HeaderNavProps) {
  const [notifOpen, setNotifOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);

  return (
    <nav className="navbar navbar-expand-lg navbar-light _header_nav _padd_t10">
      <div className="container _custom_container">
        <div className="_logo_wrap">
          <img src="/assets/images/logo.svg" alt="News Feed" className="_nav_logo" />
        </div>

        <div className="_header_form ms-auto">
          <form className="_header_form_grp" onSubmit={(e) => e.preventDefault()}>
            <SearchIcon className="_header_form_svg" />
            <input className="form-control me-2 _inpt1" type="search" placeholder="input search text" aria-label="Search" />
          </form>
        </div>

        <ul className="navbar-nav mb-2 mb-lg-0 _header_nav_list ms-auto _mar_r8">
          <li className="nav-item _header_nav_item">
            <a className="nav-link _header_nav_link_active _header_nav_link" aria-current="page" href="#0">
              <HomeIcon />
            </a>
          </li>
          <li className="nav-item _header_nav_item">
            <a className="nav-link _header_nav_link" href="#0">
              <FriendRequestIcon />
            </a>
          </li>
          <li className="nav-item _header_nav_item">
            <span className="nav-link _header_nav_link _header_notify_btn" onClick={() => setNotifOpen((v) => !v)}>
              <BellIcon />
              <span className="_counting">{NOTIFICATIONS.length}</span>
              <div className={`_notification_dropdown${notifOpen ? " show" : ""}`}>
                <div className="_notifications_content">
                  <h4 className="_notifications_content_title">Notifications</h4>
                  <div className="_notification_box_right">
                    <button type="button" className="_notification_box_right_link">
                      <KebabIcon />
                    </button>
                    <div className="_notifications_drop_right">
                      <ul className="_notification_list">
                        <li className="_notification_item">
                          <span className="_notification_link">Mark as all read</span>
                        </li>
                        <li className="_notification_item">
                          <span className="_notification_link">Notification settings</span>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
                <div className="_notifications_drop_box">
                  <div className="_notifications_drop_btn_grp">
                    <button className="_notifications_btn_link">All</button>
                    <button className="_notifications_btn_link1">Unread</button>
                  </div>
                  <div className="_notifications_all">
                    {NOTIFICATIONS.map((n, i) => (
                      <div key={i} className="_notification_box">
                        <div className="_notification_image">
                          <img src={n.image} alt="" className="_notify_img" />
                        </div>
                        <div className="_notification_txt">
                          <p className="_notification_para">
                            <span className="_notify_txt_link">{n.name}</span> {n.text}
                          </p>
                          <div className="_nitification_time">
                            <span>{n.time}</span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </span>
          </li>
          <li className="nav-item _header_nav_item">
            <a className="nav-link _header_nav_link" href="#0">
              <ChatIcon /> <span className="_counting">2</span>
            </a>
          </li>
        </ul>

        <div className="_header_nav_profile" style={{ position: "relative" }}>
          <div className="_header_nav_profile_image">
            <img src={user?.avatarUrl ?? "/assets/images/profile.png"} alt="" className="_nav_profile_img" />
          </div>
          <div className="_header_nav_dropdown">
            <p className="_header_nav_para">{user ? `${user.firstName} ${user.lastName}` : ""}</p>
            <button className="_header_nav_dropdown_btn _dropdown_toggle" type="button" onClick={() => setProfileOpen((v) => !v)}>
              <CaretDownIcon />
            </button>
          </div>

          <div className={`_nav_profile_dropdown _profile_dropdown${profileOpen ? " show" : ""}`}>
            <div className="_nav_profile_dropdown_info">
              <div className="_nav_profile_dropdown_image">
                <img src={user?.avatarUrl ?? "/assets/images/profile.png"} alt="" className="_nav_drop_img" />
              </div>
              <div className="_nav_profile_dropdown_info_txt">
                <h4 className="_nav_dropdown_title">{user ? `${user.firstName} ${user.lastName}` : ""}</h4>
              </div>
            </div>
            <hr />
            <ul className="_nav_dropdown_list">
              <li className="_nav_dropdown_list_item">
                <a href="#0" className="_nav_dropdown_link">
                  <div className="_nav_drop_info">Settings</div>
                </a>
              </li>
              <li className="_nav_dropdown_list_item">
                <a href="#0" className="_nav_dropdown_link">
                  <div className="_nav_drop_info">Help &amp; Support</div>
                </a>
              </li>
              <li className="_nav_dropdown_list_item">
                <button type="button" className="_nav_dropdown_link" onClick={onLogout} style={{ background: "none", border: "none", width: "100%", textAlign: "left" }}>
                  <div className="_nav_drop_info">Log Out</div>
                </button>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </nav>
  );
}
