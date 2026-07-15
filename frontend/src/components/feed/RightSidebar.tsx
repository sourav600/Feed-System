import { SearchIcon, OnlineDotIcon } from "./icons";

const YOU_MIGHT_LIKE = { name: "Radovan SkillArena", title: "Founder & CEO at Trophy", image: "/assets/images/Avatar.png" };

const FRIENDS = [
  { name: "Steve Jobs", title: "CEO of Apple", image: "/assets/images/people1.png", online: false, lastSeen: "5 minute ago" },
  { name: "Ryan Roslansky", title: "CEO of Linkedin", image: "/assets/images/people2.png", online: true },
  { name: "Dylan Field", title: "CEO of Figma", image: "/assets/images/people3.png", online: true },
  { name: "Karim Saif", title: "Product Designer", image: "/assets/images/people2.png", online: true },
  { name: "Anna Kade", title: "Frontend Engineer", image: "/assets/images/people3.png", online: false, lastSeen: "1 hour ago" },
];

export function RightSidebar() {
  return (
    <div className="col-xl-3 col-lg-3 col-md-12 col-sm-12">
      <div className="_layout_right_sidebar_wrap">
        <div className="_layout_right_sidebar_inner">
          <div className="_right_inner_area_info _padd_t24 _padd_b24 _padd_r24 _padd_l24 _b_radious6 _feed_inner_area">
            <div className="_right_inner_area_info_content _mar_b24">
              <h4 className="_right_inner_area_info_content_title _title5">You Might Like</h4>
              <span className="_right_inner_area_info_content_txt">
                <a className="_right_inner_area_info_content_txt_link" href="#0">
                  See All
                </a>
              </span>
            </div>
            <hr className="_underline" />
            <div className="_right_inner_area_info_ppl">
              <div className="_right_inner_area_info_box">
                <div className="_right_inner_area_info_box_image">
                  <img src={YOU_MIGHT_LIKE.image} alt="" className="_ppl_img" />
                </div>
                <div className="_right_inner_area_info_box_txt">
                  <h4 className="_right_inner_area_info_box_title">{YOU_MIGHT_LIKE.name}</h4>
                  <p className="_right_inner_area_info_box_para">{YOU_MIGHT_LIKE.title}</p>
                </div>
              </div>
              <div className="_right_info_btn_grp">
                <button type="button" className="_right_info_btn_link">
                  Ignore
                </button>
                <button type="button" className="_right_info_btn_link _right_info_btn_link_active">
                  Follow
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="_layout_right_sidebar_inner">
          <div className="_feed_right_inner_area_card _padd_t24 _padd_b6 _padd_r24 _padd_l24 _b_radious6 _feed_inner_area">
            <div className="_feed_top_fixed">
              <div className="_feed_right_inner_area_card_content _mar_b24">
                <h4 className="_feed_right_inner_area_card_content_title _title5">Your Friends</h4>
                <span className="_feed_right_inner_area_card_content_txt">
                  <a className="_feed_right_inner_area_card_content_txt_link" href="#0">
                    See All
                  </a>
                </span>
              </div>
              <form className="_feed_right_inner_area_card_form" onSubmit={(e) => e.preventDefault()}>
                <SearchIcon className="_feed_right_inner_area_card_form_svg" />
                <input className="form-control me-2 _feed_right_inner_area_card_form_inpt" type="search" placeholder="input search text" aria-label="Search" />
              </form>
            </div>
            <div className="_feed_bottom_fixed">
              {FRIENDS.map((friend) => (
                <div key={friend.name} className={`_feed_right_inner_area_card_ppl${!friend.online ? " _feed_right_inner_area_card_ppl_inactive" : ""}`}>
                  <div className="_feed_right_inner_area_card_ppl_box">
                    <div className="_feed_right_inner_area_card_ppl_image">
                      <img src={friend.image} alt="" className="_box_ppl_img" />
                    </div>
                    <div className="_feed_right_inner_area_card_ppl_txt">
                      <h4 className="_feed_right_inner_area_card_ppl_title">{friend.name}</h4>
                      <p className="_feed_right_inner_area_card_ppl_para">{friend.title}</p>
                    </div>
                  </div>
                  <div className="_feed_right_inner_area_card_ppl_side">
                    {friend.online ? <OnlineDotIcon /> : <span>{friend.lastSeen}</span>}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
