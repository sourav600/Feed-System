const PEOPLE_STORIES = [
  { name: "Ryan Roslansky", image: "/assets/images/card_ppl2.png", mini: "/assets/images/mini_pic.png" },
  { name: "Steve Jobs", image: "/assets/images/card_ppl3.png", mini: "/assets/images/mini_pic.png" },
  { name: "Dylan Field", image: "/assets/images/card_ppl4.png", mini: "/assets/images/mini_pic.png" },
];

export function StoriesBar() {
  return (
    <>
      <div className="_feed_inner_ppl_card _mar_b16">
      <div className="row">
        <div className="col-xl-3 col-lg-3 col-md-4 col-sm-4 col">
          <div className="_feed_inner_profile_story _b_radious6">
            <div className="_feed_inner_profile_story_image">
              <img src="/assets/images/card_ppl1.png" alt="" className="_profile_story_img" />
              <div className="_feed_inner_story_txt">
                <div className="_feed_inner_story_btn">
                  <button type="button" className="_feed_inner_story_btn_link">
                    <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" fill="none" viewBox="0 0 10 10">
                      <path stroke="#fff" strokeLinecap="round" d="M.5 4.884h9M4.884 9.5v-9" />
                    </svg>
                  </button>
                </div>
                <p className="_feed_inner_story_para">Your Story</p>
              </div>
            </div>
          </div>
        </div>
        {PEOPLE_STORIES.map((person, index) => (
          <div key={person.name} className={`col-xl-3 col-lg-3 col-md-4 col-sm-4${index >= 1 ? " _custom_mobile_none" : ""}`}>
            <div className="_feed_inner_public_story _b_radious6">
              <div className="_feed_inner_public_story_image">
                <img src={person.image} alt="" className="_public_story_img" />
                <div className="_feed_inner_pulic_story_txt">
                  <p className="_feed_inner_pulic_story_para">{person.name}</p>
                </div>
                <div className="_feed_inner_public_mini">
                  <img src={person.mini} alt="" className="_public_mini_img" />
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
      </div>

      <div className="_feed_inner_ppl_card_mobile _mar_b16">
        <div style={{ display: "flex", overflowX: "auto", gap: 12 }}>
          <div className="_feed_inner_profile_story _b_radious6" style={{ flex: "0 0 110px", width: 110, height: 160 }}>
            <div className="_feed_inner_profile_story_image" style={{ height: "100%" }}>
              <img src="/assets/images/card_ppl1.png" alt="" className="_profile_story_img" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
              <div className="_feed_inner_story_txt">
                <div className="_feed_inner_story_btn">
                  <button type="button" className="_feed_inner_story_btn_link">
                    <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" fill="none" viewBox="0 0 10 10">
                      <path stroke="#fff" strokeLinecap="round" d="M.5 4.884h9M4.884 9.5v-9" />
                    </svg>
                  </button>
                </div>
                <p className="_feed_inner_story_para">Your Story</p>
              </div>
            </div>
          </div>
          {PEOPLE_STORIES.map((person) => (
            <div key={person.name} className="_feed_inner_public_story _b_radious6" style={{ flex: "0 0 110px", width: 110, height: 160 }}>
              <div className="_feed_inner_public_story_image" style={{ height: "100%" }}>
                <img src={person.image} alt="" className="_public_story_img" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                <div className="_feed_inner_pulic_story_txt">
                  <p className="_feed_inner_pulic_story_para">{person.name}</p>
                </div>
                <div className="_feed_inner_public_mini">
                  <img src={person.mini} alt="" className="_public_mini_img" />
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}
