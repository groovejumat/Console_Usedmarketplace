package org.techtown.consolenuri.app;

//도착 지점 표시
public class EndPoints {

    //해당 REST API를 실행하여 필요한 쿼리와 명령어를 받아온다.
    //방생성 쿼리를 restapi로 만든 후 endpoint값으로 세팅을 하여 실행하는 방법을 취해야 한다.
    public static final String BASE_URL = "http://192.168.244.105/v1";
    public static final String LOGIN = BASE_URL + "/user/login"; //기존예제에서는 로그인했을 때에 없는 정보이면 등록처리를 해버린다.
    public static final String USER = BASE_URL + "/user/_ID_";

    //채팅룸 관련 API
    public static final String CHAT_ROOMS = BASE_URL + "/chat_rooms";
    public static final String CHAT_THREAD = BASE_URL + "/chat_rooms/_ID_";
    public static final String CHAT_ROOM_MESSAGE = BASE_URL + "/chat_rooms/_ID_/message";

    //제품 정보를 불러오는 api를 여기서 만들어서 호출 시키자.
    public static final String PRODUCT_DETAIL = BASE_URL + "/product/_ID_";
    public static final String PRODUCT_ALL = BASE_URL + "/products"; //제품정보 전체를 불러옵니다.
    public static final String PRODUCT_DLELTE = BASE_URL + "/deleteproduct/_ID_";

    //채팅룸을 생성하는 api를 호출 채팅룸과 관련된 api는 여기에서 처리하도록 하기.
    public static final String CREATE_CHATROOM = BASE_URL + "/createchatroom";
    public static final String TRADE_CHATROOMS = BASE_URL + "/getUserschatrooms";
    public static final String SELLER_CHATROOMS = BASE_URL + "/getSellerchatrooms";
    public static final String ALLERT_TO_WRITER = BASE_URL + "/alerttowriter";

    //거래내역을 생성하는 api를 호출합니다.
    public static final String ADD_TRADINGPRODUCT = BASE_URL + "/tradingproduct";
    public static final String SELL_TRADINGPROCUT = BASE_URL + "/getSelltradingproducts";
    public static final String BUY_TRADINGPROCUT = BASE_URL + "/getBuytradingproducts";
    public static final String UPDATE_TRADINGPROGRESS = BASE_URL + "/getUpdatetradingprogress"; // 제품 프로그래스 정보를 업데이트 합니다.

    //거래정보를 확인하는 api를 호출합니다.
    public static final String CHECK_USER_TRADING = BASE_URL + "/checkUserTrading";

    //제품에 대한 주소지를 관리하는 API를 호출합니다.
    public static final String ADD_TRADING_ADDRESS = BASE_URL + "/addTradingAddress";
    public static final String GET_TRADING_ADDRESS = BASE_URL + "/getTradingAddress/_ID_";
}

