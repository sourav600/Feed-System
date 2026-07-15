import {
  BookmarksIcon,
  FindFriendsIcon,
  GroupIcon,
  InsightsIcon,
  LearningIcon,
  SaveIcon,
} from "./icons";

const EXPLORE_LINKS = [
  { label: "Learning", icon: LearningIcon, badge: "New" },
  { label: "Insights", icon: InsightsIcon },
  { label: "Find friends", icon: FindFriendsIcon },
  { label: "Bookmarks", icon: BookmarksIcon },
  { label: "Group", icon: GroupIcon },
  { label: "Save post", icon: SaveIcon },
];

const SUGGESTED_PEOPLE = [
  { name: "Steve Jobs", title: "CEO of Apple", image: "/assets/images/people1.png" },
  { name: "Ryan Roslansky", title: "CEO of Linkedin", image: "/assets/images/people2.png" },
  { name: "Dylan Field", title: "CEO of Figma", image: "/assets/images/people3.png" },
];

const EVENTS = [
  { date: "10", month: "Jul", title: "No more terrorism no more cry", going: "17 People Going" },
  { date: "22", month: "Aug", title: "Design systems meetup", going: "34 People Going" },
];

export function LeftSidebar() {
  return (
    <div className="col-xl-3 col-lg-3 col-md-12 col-sm-12">
      <div className="_layout_left_sidebar_wrap">
        <div className="_layout_left_sidebar_inner">
          <div className="_left_inner_area_explore _padd_t24 _padd_b6 _padd_r24 _padd_l24 _b_radious6 _feed_inner_area">
            <h4 className="_left_inner_area_explore_title _title5 _mar_b24">Explore</h4>
            <ul className="_left_inner_area_explore_list">
              {EXPLORE_LINKS.map(({ label, icon: Icon, badge }) => (
                <li key={label} className="_left_inner_area_explore_item _explore_item">
                  <a href="#0" className="_left_inner_area_explore_link">
                    <Icon />
                    {label}
                  </a>
                  {badge && <span className="_left_inner_area_explore_link_txt">{badge}</span>}
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="_layout_left_sidebar_inner">
          <div className="_left_inner_area_suggest _padd_t24 _padd_b6 _padd_r24 _padd_l24 _b_radious6 _feed_inner_area">
            <div className="_left_inner_area_suggest_content _mar_b24">
              <h4 className="_left_inner_area_suggest_content_title _title5">Suggested People</h4>
              <span className="_left_inner_area_suggest_content_txt">
                <a className="_left_inner_area_suggest_content_txt_link" href="#0">
                  See All
                </a>
              </span>
            </div>
            {SUGGESTED_PEOPLE.map((person) => (
              <div key={person.name} className="_left_inner_area_suggest_info">
                <div className="_left_inner_area_suggest_info_box">
                  <div className="_left_inner_area_suggest_info_image">
                    <img src={person.image} alt="" className="_info_img" />
                  </div>
                  <div className="_left_inner_area_suggest_info_txt">
                    <h4 className="_left_inner_area_suggest_info_title">{person.name}</h4>
                    <p className="_left_inner_area_suggest_info_para">{person.title}</p>
                  </div>
                </div>
                <div className="_left_inner_area_suggest_info_link">
                  <a href="#0" className="_info_link">
                    Connect
                  </a>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="_layout_left_sidebar_inner">
          <div className="_left_inner_area_event _padd_t24 _padd_b6 _padd_r24 _padd_l24 _b_radious6 _feed_inner_area">
            <div className="_left_inner_event_content">
              <h4 className="_left_inner_event_title _title5">Events</h4>
              <a href="#0" className="_left_inner_event_link">
                See all
              </a>
            </div>
            {EVENTS.map((event) => (
              <a key={event.title} className="_left_inner_event_card_link" href="#0">
                <div className="_left_inner_event_card">
                  <div className="_left_inner_event_card_iamge">
                    <img src="/assets/images/feed_event1.png" alt="" className="_card_img" />
                  </div>
                  <div className="_left_inner_event_card_content">
                    <div className="_left_inner_card_date">
                      <p className="_left_inner_card_date_para">{event.date}</p>
                      <p className="_left_inner_card_date_para1">{event.month}</p>
                    </div>
                    <div className="_left_inner_card_txt">
                      <h4 className="_left_inner_event_card_title">{event.title}</h4>
                    </div>
                  </div>
                  <hr className="_underline" />
                  <div className="_left_inner_event_bottom">
                    <p className="_left_iner_event_bottom">{event.going}</p>
                    <a href="#0" className="_left_iner_event_bottom_link">
                      Going
                    </a>
                  </div>
                </div>
              </a>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
