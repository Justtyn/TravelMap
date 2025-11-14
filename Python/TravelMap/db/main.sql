/*
 Navicat Premium Dump SQL

 Source Server         : TravelMap
 Source Server Type    : SQLite
 Source Server Version : 3045000 (3.45.0)
 Source Schema         : main

 Target Server Type    : SQLite
 Target Server Version : 3045000 (3.45.0)
 File Encoding         : 65001

 Date: 14/11/2025 14:15:17
*/

PRAGMA foreign_keys = false;

-- ----------------------------
-- Table structure for cart_item
-- ----------------------------
DROP TABLE IF EXISTS "cart_item";
CREATE TABLE cart_item (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER NOT NULL,
    product_id   INTEGER NOT NULL,
    quantity     INTEGER NOT NULL,
    create_time  TEXT,
    FOREIGN KEY (user_id)    REFERENCES user(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- ----------------------------
-- Records of cart_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS "favorite";
CREATE TABLE favorite (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER NOT NULL,
    target_id    INTEGER NOT NULL,
    target_type  TEXT NOT NULL,   -- SCENIC / PRODUCT
    create_time  TEXT,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- ----------------------------
-- Records of favorite
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS "order_item";
CREATE TABLE order_item (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id    INTEGER NOT NULL,
    product_id  INTEGER NOT NULL,
    quantity    INTEGER NOT NULL,
    price       REAL NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES order_main(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- ----------------------------
-- Records of order_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for order_main
-- ----------------------------
DROP TABLE IF EXISTS "order_main";
CREATE TABLE order_main (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no      TEXT NOT NULL,
    user_id       INTEGER NOT NULL,
    order_type    TEXT NOT NULL,       -- PRODUCT / HOTEL
    total_price   REAL NOT NULL,
    status        TEXT NOT NULL,       -- CREATED / PAID / CANCELLED ...
    create_time   TEXT,
    pay_time      TEXT,
    contact_name  TEXT,
    contact_phone TEXT,
    checkin_date  TEXT,                -- 酒店入住日期（可空）
    checkout_date TEXT,                -- 酒店退房日期（可空）
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- ----------------------------
-- Records of order_main
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS "product";
CREATE TABLE "product" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "name" TEXT NOT NULL,
  "scenic_id" INTEGER,
  "cover_image" TEXT,
  "price" REAL NOT NULL,
  "stock" INTEGER DEFAULT 0,
  "description" TEXT,
  "type" TEXT,
  "hotel_address" TEXT,
  FOREIGN KEY ("scenic_id") REFERENCES "scenic" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- ----------------------------
-- Records of product
-- ----------------------------
BEGIN;
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (29, '故宫午门优先票', 17, NULL, 60.0, 1200, '含午门入场与基本语音讲解，限当日使用', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (30, '天安门观礼区预约票', 18, NULL, 30.0, 800, '清晨升旗及观礼区限量席位，需要持身份证入场', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (31, '颐和园联票（含园中园）', 19, NULL, 80.0, 1500, '包含佛香阁、德和园等园中园景点，一票畅游', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (32, '八达岭夜游攀登票', 21, NULL, 180.0, 600, '夜场限定开放，含手电与保温茶水', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (33, '黄浦江夜游船票·黄金甲', 22, NULL, 298.0, 400, '90 分钟夜游，覆盖外滩至世博段，含热饮', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (34, '东方明珠高空通票', 23, NULL, 198.0, 900, '含259米透明观光廊与上海城市历史陈列馆', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (35, '上海豫园安和里精品酒店', 24, NULL, 880.0, 20, '明清宅院改造，含私房早餐与夜游向导', 'HOTEL', '上海市黄浦区福佑路115号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (36, '星愿度假酒店·亲子房', 25, NULL, 1280.0, 35, '迪士尼官方合作酒店，含乐园接驳与亲子下午茶', 'HOTEL', '上海市浦东新区星愿路88号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (37, '外滩外白渡桥江景酒店', 22, NULL, 1180.0, 18, '落地窗江景房，含夜游船登船口接送', 'HOTEL', '上海市虹口区黄浦路55号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (38, '广州塔猎德江景酒店', 27, NULL, 960.0, 25, '珠江新城核心地段，含天际线酒吧体验券', 'HOTEL', '广州市天河区猎德大道178号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (39, '白云山云麓山庄', 28, NULL, 720.0, 16, '山林度假客房，内附茶席体验', 'HOTEL', '广州市白云区云台花园侧云麓山庄');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (40, '鼓浪屿船屋民宿·海景阁', 32, NULL, 680.0, 14, '面朝日光岩海湾，附送码头接驳', 'HOTEL', '厦门市思明区鼓浪屿观海路18号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (41, '南普陀禅意客栈', 33, NULL, 580.0, 12, '素食早餐与晨钟体验，邻近南普陀寺山门', 'HOTEL', '厦门市思明区五老峰路9号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (42, '岭南早茶礼盒', NULL, NULL, 168.0, 500, '精选陈皮普洱、凤凰单枞与广式点心伴手礼', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (43, '上海城市地铁纪念卡套装', NULL, NULL, 88.0, 800, '包含磁悬浮、地铁主题卡及线路速查册', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (44, '北京城墙文创背包', NULL, NULL, 198.0, 300, '以西城砖纹理为灵感，内置隐藏卡槽', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (45, '厦门海风香氛蜡烛', NULL, NULL, 129.0, 420, '复刻鼓浪屿海风味道，50小时燃烧时长', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (46, '圆明园遗址晨雾票', 20, NULL, 55.0, 700, '含遗址博物馆参观与讲解耳机，限上午入园', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (47, '田子坊手作工作坊通票', 26, NULL, 120.0, 350, '一次性体验手工香皂、版画与咖啡拉花', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (48, '长隆海洋夜场通票', 31, NULL, 260.0, 800, '夜场巡游＋鲸鲨馆延长开放，含纪念胸章', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (49, '深圳世界之窗极速通道票', 29, NULL, 220.0, 650, '含极速通道与晚间灯光秀预留席', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (50, '清源山祈福步道票', 36, NULL, 45.0, 500, '含香牌与山门讲解，适合晨练人群', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (51, '锦绣中华民俗村全天票', 30, NULL, 180.0, 900, '含民俗演出与苗寨长桌宴体验券', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (52, '豫园湖心阁茶宿', 24, NULL, 760.0, 10, '入住江南合院客房，含夜游豫园讲解', 'HOTEL', '上海市黄浦区九曲桥路31号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (53, '外滩源 PARK Hyatt 行政江景房', 22, NULL, 1650.0, 15, '高层江景＋行政酒廊下午茶', 'HOTEL', '上海市黄浦区中山东一路199号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (54, '广州塔璀璨套房', 27, NULL, 1880.0, 8, '顶层景观浴缸与夜景套餐，含定制香薰', 'HOTEL', '广州市海珠区阅江西路222号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (55, '曾厝垵里弄美宿·露台房', 34, NULL, 520.0, 20, '露台配海风吧台，赠送手冲咖啡体验', 'HOTEL', '厦门市思明区曾厝垵西路45号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (56, '泉州开元寺香宿', 35, NULL, 480.0, 18, '闽南古厝改造，含晨钟祈福与素斋', 'HOTEL', '泉州市鲤城区开元寺西侧巷3号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (57, '外滩摄影徒步体验', 22, NULL, 268.0, 300, '专业摄影师带队捕捉清晨外滩光影，含修图', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (58, '厦门海岛骑行日票', 32, NULL, 199.0, 260, '含电助力单车与定制路线，配防晒包', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (59, '北京胡同深度文化游', 18, NULL, 299.0, 180, '三轮车+四合院私享讲解，含老北京小吃', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (60, '珠海海风帆船体验', 31, NULL, 420.0, 120, '2 小时近海帆船与教练指导，含照片纪念', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (61, '上海咖啡地图联名杯', NULL, NULL, 158.0, 450, '与独立咖啡馆联名的限定保温杯，附地图', 'TRAVEL', NULL);
COMMIT;

-- ----------------------------
-- Table structure for scenic
-- ----------------------------
DROP TABLE IF EXISTS "scenic";
CREATE TABLE "scenic" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "name" TEXT NOT NULL,
  "city" TEXT,
  "cover_image" TEXT,
  "description" TEXT,
  "address" TEXT,
  "latitude" REAL,
  "longitude" REAL,
  "audio_url" TEXT
);

-- ----------------------------
-- Records of scenic
-- ----------------------------
BEGIN;
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (17, '故宫博物院', '北京', '', '中国明清两代皇家宫殿，世界五大宫之首。', '北京市东城区景山前街4号', 39.9163, 116.3972, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (18, '天安门广场', '北京', '', '世界最大的城市广场，是北京象征性建筑群核心区域。', '北京市东城区东长安街', 39.9033, 116.3915, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (19, '颐和园', '北京', '', '皇家园林代表作，以昆明湖和万寿山为核心。', '北京市海淀区新建宫门路19号', 39.993, 116.2755, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (20, '圆明园遗址公园', '北京', '', '“万园之园”，中国园林艺术的巅峰之作。', '北京市海淀区清华西路28号', 40.008, 116.3043, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (21, '八达岭长城', '北京', '', '世界文化遗产，长城最著名的段落。', '北京市延庆区八达岭镇', 40.3653, 116.0203, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (22, '外滩', '上海', '', '黄浦江畔最具上海标志性的景观带。', '上海市黄浦区中山东一路', 31.24, 121.49, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (23, '东方明珠', '上海', '', '上海地标性电视塔，上海城市名片。', '上海市浦东新区世纪大道1号', 31.2397, 121.4998, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (24, '豫园', '上海', '', '江南古典园林代表，具有浓厚传统韵味。', '上海市黄浦区安仁街137号', 31.2271, 121.4926, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (25, '迪士尼乐园', '上海', '', '全球最大的迪士尼主题公园之一。', '上海市浦东新区川沙新镇', 31.144, 121.657, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (26, '田子坊', '上海', '', '由石库门建筑聚集而成的艺术街区。', '上海市黄浦区泰康路248弄', 31.2082, 121.4663, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (27, '广州塔', '广州', '', '中国第一高塔，广州城市地标。', '广州市海珠区阅江西路222号', 23.1065, 113.324, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (28, '白云山', '广州', '', '“羊城第一秀”，国家5A级风景区。', '广州市白云区白云大道', 23.1666, 113.293, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (29, '深圳世界之窗', '深圳', '', '世界文化微缩景观主题乐园。', '深圳市南山区深南大道9037号', 22.5333, 113.973, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (30, '锦绣中华民俗村', '深圳', '', '大型人文旅游景区，展示中国民俗文化。', '深圳市南山区华侨城', 22.5382, 113.981, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (31, '长隆海洋王国', '珠海', '', '世界最大海洋主题乐园之一。', '珠海市横琴新区富祥湾', 22.098, 113.533, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (32, '鼓浪屿', '厦门', '', '世界文化遗产，音乐与浪漫之岛。', '厦门市思明区鼓浪屿', 24.4458, 118.0703, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (33, '南普陀寺', '厦门', '', '闽南佛教名寺，背靠五老峰。', '厦门市思明区思明南路515号', 24.436, 118.096, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (34, '曾厝垵文创村', '厦门', '', '网红文创艺术村落。', '厦门市思明区曾厝垵', 24.4385, 118.131, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (35, '开元寺', '泉州', '', '福建规模最大的寺院，唐代创建。', '泉州市鲤城区西街', 24.9122, 118.588, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (36, '清源山', '泉州', '', '以石雕、山泉、文化闻名的名山。', '泉州市丰泽区清源山', 24.946, 118.607, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (37, '西湖', '杭州', '', '世界文化遗产，“人间天堂”代表。', '杭州市西湖区西湖风景区', 30.2431, 120.15, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (38, '灵隐寺', '杭州', '', '中国佛教禅宗十大古刹之一。', '杭州市西湖区灵隐路法云弄1号', 30.24, 120.1167, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (39, '宋城景区', '杭州', '', '大型宋文化主题公园。', '杭州市西湖区之江路148号', 30.1873, 120.135, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (40, '天一阁', '宁波', '', '中国现存最早的私人藏书楼。', '宁波市海曙区天一街', 29.8781, 121.548, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (41, '雁荡山', '温州', '', '中国十大名山之一，以奇峰怪石著称。', '温州市乐清市雁荡镇', 28.3866, 121.168, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (42, '中山陵', '南京', '', '伟大革命先行者孙中山先生陵寝。', '南京市玄武区紫金山南麓', 32.064, 118.85, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (43, '夫子庙秦淮河', '南京', '', '古都南京的文化与商业中心。', '南京市秦淮区秦淮河畔', 32.022, 118.8, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (44, '拙政园', '苏州', '', '江南园林之首，世界文化遗产。', '苏州市姑苏区东北街178号', 31.329, 120.633, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (45, '虎丘', '苏州', '', '苏州象征，“不到虎丘，枉到苏州”。', '苏州市姑苏区虎丘山门内8号', 31.337, 120.598, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (46, '周庄古镇', '苏州', '', '中国第一水乡。', '苏州市昆山市周庄镇', 31.118, 120.858, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (47, '宽窄巷子', '成都', '', '成都历史文化街区，茶文化聚集地。', '成都市青羊区长顺上街', 30.6667, 104.0667, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (48, '春熙路', '成都', '', '成都时尚商业中心。', '成都市锦江区春熙路商圈', 30.657, 104.08, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (49, '乐山大佛', '乐山', '', '世界最大石刻坐佛。', '乐山市市中区凌云路', 29.552, 103.772, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (50, '峨眉山', '乐山', '', '四大佛教名山之首。', '四川省峨眉山市', 29.589, 103.332, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (51, '北川羌城旅游区', '绵阳', '', '羌族文化体验区。', '绵阳市北川县', 31.791, 104.468, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (52, '大理古城', '大理', '', '白族文化核心区。', '大理市大理古城内', 25.698, 100.157, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (53, '洱海', '大理', '', '“风花雪月”的象征。', '大理市洱海生态区', 25.699, 100.23, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (54, '丽江古城', '丽江', '', '世界文化遗产，纳西族文化中心。', '丽江市古城区', 26.872, 100.238, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (55, '玉龙雪山', '丽江', '', '北半球最南的雪山。', '丽江市玉龙纳西族自治县', 27.098, 100.177, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (56, '西双版纳热带雨林', '西双版纳', '', '中国最具代表性的原生态雨林区。', '景洪市橄榄坝', 21.995, 100.803, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (57, '天涯海角', '三亚', '', '中国浪漫海岸线代表景区。', '三亚市天涯区天涯海角景区', 18.305, 109.284, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (58, '亚龙湾热带天堂森林公园', '三亚', '', '电影《非诚勿扰Ⅱ》拍摄地。', '三亚市吉阳区亚龙湾', 18.2345, 109.6167, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (59, '蜈支洲岛', '三亚', '', '潜水胜地，中国最美岛屿之一。', '三亚市海棠湾镇', 18.3231, 109.769, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (60, '海口骑楼老街', '海口', '', '海口最具特色的历史风貌街区。', '海口市龙华区中山路', 20.035, 110.349, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (61, '五指山风景区', '五指山', '', '海南最高峰，热带雨林胜地。', '五指山市五指山景区', 18.775, 109.516, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (62, '什刹海历史文化风景区', '北京', '', '集胡同、老北京院落与湖景于一体的历史文化风景区。', '北京市西城区地安门外大街什刹海沿线', 39.9406, 116.3809, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (63, '奥林匹克公园', '北京', '', '北京2008年奥运会主场馆集中区，适合散步与夜景观赏。', '北京市朝阳区北辰路奥林匹克公园', 40.0, 116.39, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (64, '北京欢乐谷', '北京', '', '大型城市主题乐园，拥有过山车、水上项目等娱乐设施。', '北京市朝阳区东四环小武基北路', 39.867, 116.499, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (65, '北京植物园', '北京', '', '集植物科研、科普和休闲观光为一体的综合性植物园。', '北京市海淀区香山路', 39.991, 116.207, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (66, '北京香山公园', '北京', '', '以红叶闻名的皇家园林，秋季观景胜地。', '北京市海淀区香山南路40号', 39.994, 116.194, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (67, '上海世纪公园', '上海', '', '上海市中心最大的城市公园之一，拥有大片草坪与湖区。', '上海市浦东新区锦绣路1001号', 31.221, 121.545, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (68, '上海海洋水族馆', '上海', '', '大型现代化水族馆，展示全球海洋生物。', '上海市浦东新区陆家嘴环路1388号', 31.241, 121.504, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (69, '上海自然博物馆', '上海', '', '以自然历史与生命演化为主题的综合博物馆。', '上海市静安区北京西路510号', 31.233, 121.462, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (70, '上海动物园', '上海', '', '市区大型动物园，适合亲子游。', '上海市长宁区虹桥路2381号', 31.191, 121.369, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (71, '共青国家森林公园', '上海', '', '城市中的森林氧吧，拥有湖泊与大片林地。', '上海市杨浦区嫩江路2000号', 31.317, 121.562, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (72, '广州荔枝湾涌', '广州', '', '水乡风情与岭南建筑融合的历史街区。', '广州市荔湾区龙津西路', 23.127, 113.235, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (73, '广州塔二沙岛公园', '广州', '', '位于珠江上的岛屿，城市绿洲与艺术空间。', '广州市越秀区二沙岛烟雨路', 23.12, 113.313, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (74, '黄埔古港文化村', '广州', '', '海上丝绸之路重要节点，保留大量岭南古建筑。', '广州市黄埔区黄埔古港路', 23.103, 113.45, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (75, '华南植物园', '广州', '', '中国最大的南亚热带植物园之一。', '广州市天河区兴科路723号', 23.183, 113.361, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (76, '东部华侨城', '深圳', '', '集山地公园、主题乐园、度假酒店于一体的大型旅游综合体。', '深圳市盐田区大梅沙东部华侨城', 22.622, 114.278, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (77, '深圳湾公园', '深圳', '', '临海滨海绿道，可远眺香港天际线。', '深圳市南山区深圳湾大道', 22.52, 113.947, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (78, '红树林自然保护区', '深圳', '', '城市中的红树林湿地生态景观。', '深圳市福田区红树林路', 22.53, 114.016, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (79, '深圳欢乐谷', '深圳', '', '综合性大型主题乐园，分为多个主题区。', '深圳市南山区侨城西街18号', 22.54, 113.985, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (80, '厦门园博苑', '厦门', '', '以海湾为载体的园林博览公园。', '厦门市集美区环杏前路', 24.614, 118.036, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (81, '集美学村', '厦门', '', '陈嘉庚先生创建的著名学村与人文景观。', '厦门市集美区集美学村', 24.575, 118.104, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (82, '胡里山炮台', '厦门', '', '清代海防炮台遗址，可远眺大海与金门。', '厦门市思明区胡里山路2号', 24.427, 118.167, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (83, '泉州台商投资区滨海公园', '泉州', '', '面向台湾海峡的滨海公园，适合看海散步。', '泉州市台商投资区滨海大道', 24.878, 118.706, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (84, '杭州宝石山', '杭州', '', '登高俯瞰西湖与市区夜景的好去处。', '杭州市西湖区曙光路宝石山路口', 30.264, 120.133, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (85, '中国茶叶博物馆', '杭州', '', '展示中国茶文化与历史的专题博物馆。', '杭州市西湖区龙井路88号', 30.218, 120.111, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (86, '苏州金鸡湖景区', '苏州', '', '现代城市湖泊景观带，夜景迷人。', '苏州市工业园区金鸡湖畔', 31.312, 120.724, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (87, '同里古镇', '苏州', '', '典型江南水乡古镇，有退思园等名园。', '苏州市吴江区同里镇', 31.155, 120.723, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (88, '南京牛首山文化旅游区', '南京', '', '佛教文化与自然山水相结合的景区。', '南京市江宁区宁丹大道18号', 31.901, 118.806, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (89, '南京老门东历史文化街区', '南京', '', '集中展示民国与明清建筑风貌的街区。', '南京市秦淮区剪子巷老门东', 32.014, 118.8, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (90, '成都人民公园', '成都', '', '体验成都慢生活与茶馆文化的城市公园。', '成都市青羊区少城路12号', 30.667, 104.063, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (91, '锦里古街', '成都', '', '以三国文化和四川民俗为主题的仿古商业街。', '成都市武侯区武侯祠大街231号', 30.643, 104.049, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (92, '重庆洪崖洞民俗风貌区', '重庆', '', '依山而建的吊脚楼群，夜景极具特色。', '重庆市渝中区嘉陵江滨江路88号', 29.5636, 106.576, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (93, '解放碑步行街', '重庆', '', '重庆最繁华的中心商业区之一。', '重庆市渝中区解放碑', 29.5573, 106.5762, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (94, '南山一棵树观景台', '重庆', '', '俯瞰重庆主城夜景的最佳观景台之一。', '重庆市南岸区南山植物园附近', 29.533, 106.61, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (95, '西安城墙永宁门', '西安', '', '保存完整的明代城墙遗址，可骑行环城。', '西安市碑林区南门外环城南路', 34.255, 108.946, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (96, '大唐芙蓉园', '西安', '', '以唐文化为背景的大型主题园林。', '西安市雁塔区曲江芙蓉西路99号', 34.21, 108.98, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (97, '曲江池遗址公园', '西安', '', '再现汉唐皇家池苑风貌的城市公园。', '西安市雁塔区曲江新区芙蓉东路', 34.219, 108.99, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (98, '昆明滇池海埂大坝', '昆明', '', '观赏红嘴鸥与高原湖泊风光的最佳地点之一。', '昆明市西山区海埂大坝', 24.954, 102.66, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (99, '石林风景区', '昆明', '', '典型喀斯特地貌风景区。', '昆明市石林彝族自治县石林镇', 24.821, 103.332, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (100, '南宁青秀山风景区', '南宁', '', '南宁城市名片之一，以佛教文化和园林景观著称。', '南宁市青秀区凤岭南路6-6号', 22.786, 108.374, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (101, '北海银滩', '北海', '', '以细腻白沙著称的海滨浴场。', '北海市银海区银滩镇', 21.445, 109.13, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (102, '三亚大东海景区', '三亚', '', '三亚最早开发的海水浴场之一。', '三亚市吉阳区大东海旅游区', 18.234, 109.513, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (103, '海口万绿园', '海口', '', '滨海大型城市公园，绿化率极高。', '海口市龙华区滨海大道', 20.034, 110.315, '');
COMMIT;

-- ----------------------------
-- Table structure for sqlite_sequence
-- ----------------------------
DROP TABLE IF EXISTS "sqlite_sequence";
CREATE TABLE sqlite_sequence(name,seq);

-- ----------------------------
-- Records of sqlite_sequence
-- ----------------------------
BEGIN;
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('scenic', 103);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('product', 61);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('visited', 10);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('trip_plan', 8);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('favorite', 15);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('cart_item', 19);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('order_main', 7);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('order_item', 10);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('user', 21);
COMMIT;

-- ----------------------------
-- Table structure for trip_plan
-- ----------------------------
DROP TABLE IF EXISTS "trip_plan";
CREATE TABLE trip_plan (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    title       TEXT,
    start_date  TEXT,
    end_date    TEXT,
    source      TEXT,      -- AI / MANUAL
    content     TEXT,      -- JSON 字符串，包含每天要去的 scenic_id 等
    create_time TEXT,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- ----------------------------
-- Records of trip_plan
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS "user";
CREATE TABLE "user" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "login_type" TEXT,
  "username" TEXT,
  "password" TEXT,
  "phone" TEXT,
  "email" TEXT,
  "nickname" TEXT,
  "avatar_url" TEXT,
  "wx_unionid" TEXT,
  "wx_openid" TEXT,
  "wx_access_token" TEXT,
  "wx_refresh_token" TEXT,
  "wx_token_expires_at" TEXT
);

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO "user" ("id", "login_type", "username", "password", "phone", "email", "nickname", "avatar_url", "wx_unionid", "wx_openid", "wx_access_token", "wx_refresh_token", "wx_token_expires_at") VALUES (20, 'LOCAL', 'test01', 'scrypt:32768:8:1$fTTGg6t3ktw2p0pu$be314cffa8eed8caa5b1632d31fed6c0bf2c159e89db9246e4b34e661f8e4507762e23f999ede07512150f10bc644171c472b442333c1a47bc7650229e05522e', '13850056409', '1046220903@qq.com', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO "user" ("id", "login_type", "username", "password", "phone", "email", "nickname", "avatar_url", "wx_unionid", "wx_openid", "wx_access_token", "wx_refresh_token", "wx_token_expires_at") VALUES (21, 'LOCAL', 'test02', 'scrypt:32768:8:1$eEnxFQteKyeooqYv$72df9d99afb19b2c49e2d21f2fd9381d83dd7becb324092ea69c332a5e212ccf558693a49bc4e34ac488cd5a74493d9a836bcc28cfe5aa59dd1c78a221b2734d', '19353540720', '44739528@qq.com', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for visited
-- ----------------------------
DROP TABLE IF EXISTS "visited";
CREATE TABLE "visited" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "user_id" INTEGER NOT NULL,
  "scenic_id" INTEGER NOT NULL,
  "visit_date" TEXT,
  "rating" INTEGER,
  FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION,
  FOREIGN KEY ("scenic_id") REFERENCES "scenic" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- ----------------------------
-- Records of visited
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Auto increment value for cart_item
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 19 WHERE name = 'cart_item';

-- ----------------------------
-- Indexes structure for table cart_item
-- ----------------------------
CREATE INDEX "main"."idx_cart_user"
ON "cart_item" (
  "user_id" ASC
);

-- ----------------------------
-- Auto increment value for favorite
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 15 WHERE name = 'favorite';

-- ----------------------------
-- Indexes structure for table favorite
-- ----------------------------
CREATE INDEX "main"."idx_favorite_user_type"
ON "favorite" (
  "user_id" ASC,
  "target_type" ASC
);

-- ----------------------------
-- Auto increment value for order_item
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 10 WHERE name = 'order_item';

-- ----------------------------
-- Auto increment value for order_main
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 7 WHERE name = 'order_main';

-- ----------------------------
-- Indexes structure for table order_main
-- ----------------------------
CREATE INDEX "main"."idx_order_user"
ON "order_main" (
  "user_id" ASC
);

-- ----------------------------
-- Auto increment value for product
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 61 WHERE name = 'product';

-- ----------------------------
-- Auto increment value for scenic
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 103 WHERE name = 'scenic';

-- ----------------------------
-- Auto increment value for trip_plan
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 8 WHERE name = 'trip_plan';

-- ----------------------------
-- Indexes structure for table trip_plan
-- ----------------------------
CREATE INDEX "main"."idx_trip_user"
ON "trip_plan" (
  "user_id" ASC
);

-- ----------------------------
-- Auto increment value for user
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 21 WHERE name = 'user';

-- ----------------------------
-- Indexes structure for table user
-- ----------------------------
CREATE INDEX "main"."idx_user_wx_openid"
ON "user" (
  "wx_openid" ASC
);

-- ----------------------------
-- Auto increment value for visited
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 10 WHERE name = 'visited';

-- ----------------------------
-- Indexes structure for table visited
-- ----------------------------
CREATE INDEX "main"."idx_visited_user"
ON "visited" (
  "user_id" ASC
);

PRAGMA foreign_keys = true;
