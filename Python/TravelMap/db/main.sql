/*
 Navicat Premium Dump SQL

 Source Server         : TravelMap
 Source Server Type    : SQLite
 Source Server Version : 3045000 (3.45.0)
 Source Schema         : main

 Target Server Type    : SQLite
 Target Server Version : 3045000 (3.45.0)
 File Encoding         : 65001

 Date: 18/11/2025 23:07:02
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
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (20, 20, 44, 1, '2025-05-10 10:05:00');
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (21, 20, 58, 2, '2025-05-10 10:06:30');
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (22, 21, 33, 2, '2025-05-09 21:15:00');
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (23, 21, 55, 1, '2025-05-09 21:20:00');
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (24, 21, 35, 3, '2025-04-22 15:06:57');
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (25, 21, 38, 3, '2025-04-30 16:53:33');
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (26, 21, 33, 1, '2025-05-04 04:29:29');
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (27, 20, 32, 2, '2025-04-16 20:16:00');
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (28, 20, 61, 1, '2025-04-30 09:18:55');
INSERT INTO "cart_item" ("id", "user_id", "product_id", "quantity", "create_time") VALUES (29, 21, 58, 1, '2025-04-27 11:55:35');
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
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (16, 20, 17, 'SCENIC', '2025-05-08 09:10:00');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (17, 20, 22, 'SCENIC', '2025-05-08 09:12:00');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (18, 20, 33, 'PRODUCT', '2025-05-08 09:15:00');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (19, 21, 32, 'SCENIC', '2025-05-06 14:05:00');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (20, 21, 55, 'PRODUCT', '2025-05-06 14:06:30');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (21, 21, 58, 'PRODUCT', '2025-05-06 14:08:00');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (22, 21, 28, 'SCENIC', '2025-04-16 16:41:30');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (23, 20, 62, 'SCENIC', '2025-04-26 11:54:12');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (24, 20, 19, 'SCENIC', '2025-05-01 19:13:22');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (25, 21, 38, 'PRODUCT', '2025-04-19 10:21:26');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (26, 20, 29, 'PRODUCT', '2025-05-09 10:08:05');
INSERT INTO "favorite" ("id", "user_id", "target_id", "target_type", "create_time") VALUES (27, 21, 44, 'PRODUCT', '2025-04-18 03:13:13');
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
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (11, 1, 37, 1, 1180.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (12, 1, 33, 1, 298.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (13, 2, 59, 2, 299.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (14, 2, 61, 1, 158.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (15, 10, 60, 2, 420.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (16, 10, 39, 1, 720.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (17, 10, 53, 1, 1650.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (18, 11, 59, 2, 299.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (19, 11, 54, 2, 1880.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (20, 11, 48, 1, 260.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (21, 12, 54, 2, 1880.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (22, 12, 59, 2, 299.0);
INSERT INTO "order_item" ("id", "order_id", "product_id", "quantity", "price") VALUES (23, 12, 39, 2, 720.0);
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
INSERT INTO "order_main" ("id", "order_no", "user_id", "order_type", "total_price", "status", "create_time", "pay_time", "contact_name", "contact_phone", "checkin_date", "checkout_date") VALUES (8, '202405071030HX20', 20, 'HOTEL', 1478.0, 'PAID', '2025-05-07 10:30:00', '2025-05-07 10:45:00', '李想', '13800001111', '2025-05-20', '2025-05-22');
INSERT INTO "order_main" ("id", "order_no", "user_id", "order_type", "total_price", "status", "create_time", "pay_time", "contact_name", "contact_phone", "checkin_date", "checkout_date") VALUES (9, '202405061215CM21', 21, 'PRODUCT', 756.0, 'CREATED', '2025-05-06 12:15:00', NULL, '陈微', '13911112222', NULL, NULL);
INSERT INTO "order_main" ("id", "order_no", "user_id", "order_type", "total_price", "status", "create_time", "pay_time", "contact_name", "contact_phone", "checkin_date", "checkout_date") VALUES (10, '202511141428121610', 20, 'HOTEL', 3210.0, 'PAID', '2025-05-10 20:45:36', '2025-05-10 20:45:36', '陈曦', '13783538629', '2025-05-28', '2025-05-30');
INSERT INTO "order_main" ("id", "order_no", "user_id", "order_type", "total_price", "status", "create_time", "pay_time", "contact_name", "contact_phone", "checkin_date", "checkout_date") VALUES (11, '202511141428125001', 21, 'PRODUCT', 4618.0, 'PAID', '2025-05-09 22:35:12', '2025-05-09 22:35:12', '张蕾', '13520012306', NULL, NULL);
INSERT INTO "order_main" ("id", "order_no", "user_id", "order_type", "total_price", "status", "create_time", "pay_time", "contact_name", "contact_phone", "checkin_date", "checkout_date") VALUES (12, '202511141428129572', 21, 'HOTEL', 5798.0, 'PAID', '2025-05-08 03:07:28', '2025-05-08 03:07:28', '林峰', '13669481643', '2025-06-03', '2025-06-06');
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
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (29, '故宫午门优先票', 17, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/故宫午门优先票.jpg', 60.0, 1200, '故宫午门优先票让您的紫禁城之旅从最庄严的宫门开始，无需在常规入口排长队，通过专属通道快速进入这座六百年历史的皇家宫殿。

持票者可在午门东侧指定检票口直接验证入场，平均节省半小时以上的等候时间，随即领取多语言语音导览设备，经过金水桥与五凤楼时自动播放相应历史解说。

漫步于太和殿、中和殿与保和殿区域，您能从容欣赏建筑细节，感受昔日百官早朝的庄重氛围。

特别适合行程紧凑的游客、携带家庭成员的参观者，或希望避开高峰人流的旅行者，在有限的游览时间内更深入地体验故宫的宏伟与故事。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (30, '天安门观礼区预约票', 18, 'https://images.pexels.com/photos/6109670/pexels-photo-6109670.jpeg', 30.0, 800, '天安门观礼区预约票让你在升旗仪式中获得更沉浸的体验。

这个位于广场北侧、距离旗杆约50米的区域，每日仅开放200个名额，需提前预约并凭身份证核验入场。

站在汉白玉栏杆旁，你能清晰看到国旗班战士踏着整齐步伐跨过金水桥，在晨光中见证五星红旗冉冉升起。

建议提前抵达，看天安门城楼的轮廓在晨雾中逐渐清晰，四周自发响起的国歌合唱会让这个清晨格外难忘。

通过TravelMap预约时，系统会根据当日日出时间智能推荐抵达时段，并附送电子纪念证书记录这个特殊时刻。

无论是家庭出游还是纪念日行程，这里都是感受国家仪式感的独特选择。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (31, '颐和园联票（含园中园）', 19, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/颐和园联票（含园中园）.jpg', 80.0, 1500, '手持颐和园联票，您将轻松步入这座世界文化遗产的每个精华角落，不仅涵盖主园区，还能从容探访佛香阁与德和园等园中园景点。

沿着昆明湖畔漫步，登上佛香阁远眺湖山相映的景致，感受昔日皇家在此祈福的宁静氛围；步入德和园，仰望高达21米的清代大戏楼，细味古建筑中蕴含的声学智慧与宴乐风华。

联票让您省去多次排队购票的繁琐，无论是与家人共度悠闲一日，还是独自沉浸于历史空间，都能更自在地领略颐和园作为清代皇家园林的营造格局与文化细节，完整感受从宗教建筑到娱乐场所的功能脉络，享受一段充实而流畅的游览时光。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (32, '八达岭夜游攀登票', 21, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/八达岭夜游攀登票.jpg', 180.0, 600, '当夕阳的余晖渐渐隐去，八达岭长城在夜色中展现出与白天截然不同的魅力。

这张夜游攀登票不仅让您避开白天的拥挤，还能在专业防滑手电的柔和光线下，细细观察砖墙上历经六百年的浮雕纹样。

随票附赠的保温壶里装着应季热饮，秋冬是暖身的红枣茶，春夏则是清新的茉莉花茶，当您在北四楼稍作休息时，抿一口温热的茶汤，仰头便能望见北斗七星悬于垛口之上的动人画面。

夜风轻拂过松林，远处定陵的灯火与蜿蜒的城灯相互映衬，仿佛两条星河在天地间对话。

建议在日落前后一小时开始登城，既能捕捉昼夜交替的蓝调时刻，又能沿着错峰路线舒适游览。

请注意穿着防滑鞋并带件薄外套，城楼间的温差较为明显。

每日限量800张的夜游票需提前实名预约，这份独特的体验将带您走进一段被星光重新诠释的历史。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (33, '黄浦江夜游船票·黄金甲', 22, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/黄浦江夜游船票·黄金甲.jpg', 298.0, 400, '暮色降临黄浦江时，登上黄金甲游船，从外滩码头缓缓启航，你将开启一段九十分钟的夜游之旅。

游船沿着浦江向南行驶，经过灯火辉煌的万国建筑群和陆家嘴摩天楼群，直至世博园区水域后折返，沿途可欣赏南浦大桥、卢浦大桥等城市地标。

船上设有双侧通透甲板，无论站在哪个位置，都能获得开阔的观景视野，尤其推荐在船尾捕捉外白渡桥与东方明珠同框的经典画面。

秋夜江风微凉，一杯热咖啡或红茶正好为你驱散寒意，让你更从容地沉浸于两岸流动的灯火之中。

航行期间，扫描座椅上的二维码即可收听中英文景点讲解，了解每一座建筑背后的故事，让这段夜航不仅是视觉的享受，也成为一次认识上海过去与现在的生动旅程。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (34, '东方明珠高空通票', 23, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/东方明珠高空通票.jpg', 198.0, 900, '登上东方明珠259米高的透明观光廊，脚下是川流不息的车河与蜿蜒的黄浦江，玻璃地面清晰映出陆家嘴摩天楼群的轮廓，仿佛漫步云端。

这份通票还包含上海城市历史陈列馆的参观权限，从外滩老建筑的微缩场景到石库门里弄的生活复原，七千余件展品串联起上海开埠至今的变迁历程。

建议在天气晴好的黄昏时段登塔，既能捕捉白昼转入夜幕时分的城市光影变化，又能避开午间密集的人流。

陈列馆内常设沉浸式展览，可作为室内行程的补充，让你在一天之内既能俯瞰现代都市全景，又能深入感受这座城市的历史脉络。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (35, '上海豫园安和里精品酒店', 24, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/上海豫园安和里精品酒店.jpg', 880.0, 20, '漫步在上海豫园安和里精品酒店，仿佛置身于一座明清时期的江南宅院，青砖黛瓦与雕花木窗静静诉说着历史韵味，与豫园园林的古雅景致浑然一体。

每天清晨，你可以在私房早餐的香气中醒来，品尝地道本帮点心，感受老上海悠闲的生活节奏；傍晚时分，跟随专属向导探访豫园夜间开放区域，避开白天的拥挤，在月色与灯笼光影中聆听园林四百年的故事。

无论你是想沉浸于文化氛围，还是寻找闹市中的宁静角落，这里都能为你的上海之行增添一份诗意与深度，步行即可抵达城隍庙和外滩，让旅程既便捷又充满记忆点。

', 'HOTEL', '上海市黄浦区福佑路115号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (36, '星愿度假酒店·亲子房', 25, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/星愿度假酒店·亲子房.jpg', 1280.0, 35, '星愿度假酒店亲子房是上海迪士尼度假区官方合作的住宿选择，让童话体验从乐园延续到休憩时光。

每天从清晨到夜晚，专属接驳车频繁往返酒店与乐园，大约十分钟即可抵达，为家庭节省宝贵的游玩时间。

客房内精心布置了迪士尼主题元素，孩子们可以抱着跳跳虎抱枕入睡，或在AR故事投影的陪伴下进入梦乡。

下午茶时段提供米奇造型甜点和特调饮品，全家一起享用，平添几分温馨与乐趣。

房间内的家具均通过国际安全认证，配有独立儿童卫浴和消毒设备，确保小客人的舒适与安全。

无论是烟花秀后的宁静夜晚，还是充满期待的清晨出发，这里都为家庭游客打造了便捷又充满魔法的住宿体验。

', 'HOTEL', '上海市浦东新区星愿路88号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (37, '外滩外白渡桥江景酒店', 22, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/外滩外白渡桥江景酒店.jpg', 1180.0, 18, '住在外滩外白渡桥江景酒店，每个房间的落地窗都正对着黄浦江，无论清晨江雾弥漫还是傍晚华灯初上，陆家嘴的天际线和外滩历史建筑群始终在你眼前展开。

这座百年铁桥曾是经典影视取景地，承载着上海开埠以来的记忆，而酒店就坐落在桥畔，距离夜游船码头仅一百多米，每天傍晚至夜间提供定时接驳车，实测六分钟即可直达登船口，比常规路线节省超过八成时间。

当其他游客还在为交通辗转时，你已从容穿过外白渡桥的钢铁骨架，融入浦江夜色。

无论是商务出行时在晨光中处理工作，还是与家人一起辨认对岸的东方明珠轮廓，这里不仅提供舒适住宿，更让你以最从容的方式体验外滩的日夜流转。

', 'HOTEL', '上海市虹口区黄浦路55号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (38, '广州塔猎德江景酒店', 27, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/广州塔猎德江景酒店.jpg', 960.0, 25, '站在广州塔猎德江景酒店的落地窗前，珠江新城的璀璨灯火如流动画卷般铺展于脚下，这里不仅是城市中轴线的核心腹地，更是一座悬浮于空中的观景台——清晨时，晨雾中的珠江泛着金色涟漪；入夜后，广州塔与东西塔构成的三塔辉映景象在窗前完整呈现。

酒店特别准备的天际线酒吧体验券将城市探索延伸至云端，乘电梯抵达107层，手持特调鸡尾酒倚在观景台栏杆上，脚下猎德大桥的车流仿佛化作银河，琶洲会展中心的玻璃幕墙正映出晚霞余晖。

客房均配备智能控光系统与隔音玻璃，在保障隐私的同时让你独享180度江景，步行8分钟可达猎德地铁站，15分钟车程覆盖花城广场等核心地标。

无论是结束珠江夜游后前往高空酒吧续写城市天际线记忆，还是在42平方米的舒适空间中用汉斯格雅花洒洗去疲惫，这里都能为想深度体验广州现代脉动的你，提供兼具便利与意境的旅居选择。

', 'HOTEL', '广州市天河区猎德大道178号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (39, '白云山云麓山庄', 28, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/白云山云麓山庄.jpg', 720.0, 16, '清晨推开窗，松涛与茶香一同涌入，这是白云山云麓山庄独有的问候。

坐落在海拔380米的山腰间，客房以原木和青石为基调，与窗外绵延的岭南丘陵自然相融。

每间42平方米的空间里设有独立的茶艺区，配备乌金石茶盘和精选茶具，赠送两泡特级凤凰单丛，你可以亲手冲泡，看蒸腾的水雾与山间流云交织。

午后循石阶探访摩星岭或漫步至山顶广场，归来时管家已备好茶点；夜晚则在露台用天文望远镜捕捉城市中罕见的星空。

客房采用地暖与隔音设计，确保山居的干爽宁静，步行可达云台花园，房费已含景区门票与观光车接驳。

无论是商务小聚、家庭度假，还是静享茶韵，这里都能让旅途浸润在自然与文化的从容节奏中。

', 'HOTEL', '广州市白云区云台花园侧云麓山庄');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (40, '鼓浪屿船屋民宿·海景阁', 32, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/鼓浪屿船屋民宿·海景阁.jpg', 680.0, 14, '这座上世纪二十年代由侨商建造的船形别墅坐落在鼓浪屿鹿礁路与福建路交界的坡地上，推开木百叶窗，日光岩就在三百八十米外清晰可见。

每天清晨，你可以在私人露台欣赏日出缓缓染红岩石的景致，午后泡一壶本地乌龙茶，看白鹭掠过对岸八卦楼的红砖穹顶。

管家会提前在三丘田或内厝澳码头等候，用电瓶车接你穿过龙眼树掩映的小道，免去拖行李爬坡的困扰。

四米二挑高的阁楼采用专业静音设计，既保留了花岗岩墙体的历史韵味，又确保了安静的休息环境。

傍晚时分，钢琴博物馆飘来的隐约旋律与潮声相伴，让人真正沉浸在这座音乐之岛的独特氛围中。

', 'HOTEL', '厦门市思明区鼓浪屿观海路18号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (41, '南普陀禅意客栈', 33, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/南普陀禅意客栈.jpg', 580.0, 12, '隐在南普陀寺山门旁榕荫下的禅意客栈，距寺院仅八十步之遥，与放生池仅一墙之隔。

每日晨光未透时，住客可随着专人引导的晨钟声缓步穿过专属通道，避开拥挤的香客直接抵达寺院第三进院落参与早课。

返回时，由南普陀素菜馆每日配送的定食早餐已静候房中，手作豆腐与五谷粥的温热正好抚慰清晨的脾胃。

三十二间客房均采用双层中空玻璃阻隔外界喧闹，露台正对古寺飞檐，午后可在此慢品闽南工夫茶，暮色中静观寺墙灯笼次第亮起。

若想体验完整的禅修日程，提前预约便可穿着备好的素色禅修服参与早晚课，在钟声与梵唱间感受千年古刹的日常韵律。

', 'HOTEL', '厦门市思明区五老峰路9号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (42, '岭南早茶礼盒', NULL, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/岭南早茶礼盒.jpg', 168.0, 500, '岭南早茶礼盒将地道广式饮茶体验融入日常，精选新会陈皮与云南普洱拼配的十五年陈化熟茶，醇厚顺滑，尤其适合在晨起后或餐后搭配点心饮用；潮州凤凰山产的凤凰单枞乌龙经传统炭焙，蜜兰香持久，午后冲泡提神生津，耐泡度高。

随盒搭配手工鸡仔饼与杏仁饼，采用猪油起酥工艺，复热后酥香更显，无论是独自在书房慢品，还是与亲友共享茶叙，独立密封包装都能让茶点保持原味。

点心适合微波加热，茶品可用盖碗或玻璃壶轻松冲泡，附赠手提袋便于赠礼，礼盒内还附有茶点制作视频链接，让您随时随地感受岭南早茶文化的闲适与温情。

', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (43, '上海城市地铁纪念卡套装', NULL, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/上海城市地铁纪念卡套装.jpg', 88.0, 800, '这套上海城市地铁纪念卡套装将实用与纪念巧妙结合，让你在探索这座城市时多一份从容。

磁悬浮主题卡能带你亲身体验那段从浦东机场到龙阳路的极速旅程，感受现代交通的流畅高效；地铁主题卡覆盖全市主要线路，每张卡片上的地标图案，从外滩的璀璨到豫园的雅致，都让日常出行变成一次次微型的城市巡礼。

随附的线路速查册以清晰的图示和站点信息，帮你快速规划路线——无论是赶早穿梭于老城街巷，还是深夜感受都市脉动，它都能默默陪伴左右。

这套纪念卡既适合日常使用，也值得收藏或赠予友人，让每一次刷卡都成为对上海快慢交织生活的温暖回味。

', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (44, '北京城墙文创背包', NULL, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/北京城墙文创背包.jpg', 198.0, 300, '这款北京城墙文创背包从西城砖的岁月纹理中汲取灵感，将斑驳的砖石质感融入日常设计，让你在触摸背包表面时仿佛能感受到古老城墙的厚重历史。

它采用耐磨的涤纶面料，通过精细压纹还原砖块的凹凸细节，不仅视觉上充满故事感，更具备良好的防水与耐用性。

背包内部的隐藏卡槽设计得十分贴心，游览故宫、天坛或漫步胡同时，可以快速取用证件或交通卡，省去翻找的麻烦。

无论是作为文化探索的伴侣，还是日常通勤的实用装备，它都能轻便陪伴你的城市行走，让历史悄然融入现代生活，既承载记忆，也满足便利。

', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (45, '厦门海风香氛蜡烛', NULL, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/厦门海风香氛蜡烛.jpg', 129.0, 420, '这款厦门海风香氛蜡烛以天然大豆蜡为基底，将鼓浪屿特有的海风气息封存于玻璃罐中。

点燃时，海盐的微咸与白花的清雅交织，仿佛置身于日光岩下的傍晚，海风裹挟着湿润水汽与岛上花香轻轻拂面。

烛火摇曳中，它能陪伴你度过五十小时的宁静时光，无论是阅读几本闲书，还是在深夜工作间隙，都能借这一缕闽南海风重构片刻安宁。

蜡烛采用纯棉烛芯，建议在十五至二十平米的空间使用，首次点燃两小时可形成完整蜡池，日常搭配防尘盖能更好地保留香气。

', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (46, '圆明园遗址晨雾票', 20, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/圆明园遗址晨雾票.jpg', 55.0, 700, '圆明园遗址晨雾票为您开启一段独特的清晨时光，让您在晨雾缭绕的七点至十点间优先入园，避开白日的拥挤人潮。

持票漫步于西洋楼残垣与海晏堂遗址之间，专业讲解耳机会伴随您的脚步，娓娓道来每处遗迹背后的历史故事，仿佛时光在此刻缓缓回流。

您还可以凭票参观常设遗址博物馆，近距离观赏青铜兽首等珍贵文物，感受岁月留下的痕迹。

这一安排不仅适合喜爱安静探索的游客，也为摄影爱好者提供了晨光与薄雾交织的绝佳取景时刻。

建议提前预约并穿着便于行走的鞋子，在参观结束后，您还能凭票根享受园内咖啡馆的特别优惠，为这段宁静的历史之旅增添一份惬意。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (47, '田子坊手作工作坊通票', 26, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/田子坊手作工作坊通票.jpg', 120.0, 350, '这张田子坊手作工作坊通票让你在充满海派风情的石库门弄堂里，亲手体验三种不同的创作乐趣。

走进天然手工皂工坊，用橄榄油和植物精油调配出适合日常使用的洁面皂；来到版画空间，在椴木板上雕刻石库门或外白渡桥的轮廓，印制成一幅可带回家的装饰画；最后在飘着咖啡香的工作室，学习打发奶泡和拉花技巧，为现磨咖啡点缀一片树叶或心形图案。

每个工作室都位于田子坊核心区域，步行几分钟即可转换，材料工具一应俱全，零基础也能轻松上手。

适合想要放慢节奏、在午后时光里感受手作温度的游客，这些亲手完成的作品会比普通纪念品更生动地留住上海的独特记忆。

建议提前一天预约，通票有效期至2024年底。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (48, '长隆海洋夜场通票', 31, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/长隆海洋夜场通票.jpg', 260.0, 800, '当夕阳为长隆海洋王国的城堡镀上金边，属于夜晚的奇妙旅程正悄然开启。

选择夜场通票不仅避开了白天的拥挤，更让您拥有四小时的专属时光——从下午四点入园起，恰好能遇见海洋夜光巡游的发光水母花车随音乐摇曳，工作人员会为持票游客戴上限定夜光手环。

此时大型游乐设施的排队时间大幅缩短，您可以在鹦鹉过山车上感受晚风，随后漫步至亮起幽蓝灯光的鲸鲨馆。

这座吉尼斯认证的全球最大水族馆在夜晚呈现出不同面貌，当普通游客陆续离开，您仍可独自驻足在10米深的巨幕前，看八条鲸鲨与数万尾黄金鲹在模拟月光中游弋，甚至能与窗后的魔鬼鱼静静对视。

随票附赠的夜光鲸鲨胸章不仅是行程纪念，还可在园区主题商店享受购物优惠，这样的安排既充实又从容，让每一次转身都遇见海洋的不同表情。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (49, '深圳世界之窗极速通道票', 29, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/深圳世界之窗极速通道票.jpg', 220.0, 650, '这张深圳世界之窗极速通道票让你在浓缩了全球130处名胜的微缩景观中轻松穿行，无需在热门景点前长时间排队，平均能节省七成等候时间。

傍晚时分从专属通道直接抵达埃菲尔铁塔前的灯光秀预留席位，阶梯式观景台高出普通区域一米多，视野开阔无遮挡，每晚七点半开始的《奇幻欧洲》光影表演将世界著名建筑轮廓点亮成流动的星河。

无论是想利用下班后时光短暂放松的上班族，还是计划带家人度过悠闲周末的父母，都能在三个小时内从容游览五大洲标志性景观，从日本园的最后一扬和服表演到泰姬陵蓝调时刻的剪影拍摄，最后手捧德国啤酒屋的饮品，在璀璨灯光与异国民谣中收获一段不必远行却充满惊喜的环球微旅行。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (50, '清源山祈福步道票', 36, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/清源山祈福步道票.jpg', 45.0, 500, '清晨六点半的薄雾还未散尽，祈福步道的青石板已映出晨光，这张门票不仅是登山凭证，更是一段文化体验的开始。

在古山门前听守山人讲述七百年香火故事，掌心那枚天然檀木香牌随着登山步伐轻轻晃动，松香与晨露的气息交织在一起。

专为晨间活动设计，登顶约需四十分钟，阶梯坡度经过实测适合晨练强度，适合赶早的晨练者在天光未大亮时独享整座山的宁静。

登顶时恰好能看见泉州城在朝霞中苏醒的全景，建议搭配APP内的晨曦摄影地图，在三大观景平台记录晨光云海。

每周三都来此处的泉州居民老陈说，这比普通健身房有意思多了，既能活动筋骨，又能带着寓意平安的香牌回家，让晨练成为兼具身心滋养的日常仪式。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (51, '锦绣中华民俗村全天票', 30, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/锦绣中华民俗村全天票.jpg', 180.0, 900, '这张锦绣中华民俗村全天票让您从早到晚深度体验中华文化的多元魅力。

清晨踏入园区，可以在微缩景区欣赏长城与漓江的精致景观，午后走进实景村落，感受佤族木鼓的节奏和傣家竹楼的宁静。

傍晚时分凭票参与苗寨长桌宴，品尝六道特色菜肴和迎宾酒水，席间还能欣赏原生态歌舞表演。

夜间可观看《东方霓裳》或《大漠传奇》两场定点演出，建议提前到场选择最佳观赏位置。

票务已接入智能导览系统，扫码即可获取实时演出排期和等候提示，帮助您高效规划全天行程。

从微缩山水到民族风情，这张通票将带您一日穿越千年文明。

', 'TICKET', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (52, '豫园湖心阁茶宿', 24, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/豫园湖心阁茶宿.jpg', 760.0, 10, '漫步穿过豫园九曲桥的灯笼光影，湖心阁茶宿静静立在荷花池畔，这座由明代园林改建的江南合院保留了青砖黛瓦的原始结构，推开雕花木门便能看见天井里的老石榴树。

房内布置着苏作明式家具，每晚八点专属导览员会带你夜探豫园，当游客散尽，你将听到四百岁玉玲珑假山如何在上海变迁中幸存，在月洞门前听讲解员还原潘允端建园时的漕运往事。

晨起时，管家会在茶亭用虎跑泉水冲泡狮峰龙井，配着沈大成定制的桂花定胜糕，看锦鲤搅碎一池倒影。

这套茶宿体验包含三大核心价值：首先入住经文物部门认证的修复合院，卧室配备地暖与隔音系统；其次专属夜游避开日间拥挤，讲解员持有文旅局颁发的豫园特级导游证；最后是沉浸式茶文化体验——次日清晨在临水平台享用双人茶席，附赠由豫园文创开发的茶器伴手礼。

特别适合想要深度了解海派园林文化，又注重私密性与专业度的旅客，目前仅开放六间客房需提前三日预约。

该住宿产品位于豫园景区腹地的湖心阁建筑群，为三进式江南合院结构。

服务包含：入住经防潮处理的传统木构客房（面积28-45㎡）、19:30-20:45的专场夜游（含电子耳麦接收器）以及次日茶艺体验（可选6:30/7:30两个时段）。

注意事项：合院区与豫园主景区有独立通道，需在闭园前1小时办理入住；夜游路线涵盖玉华堂-得月楼-静观大厅三处重点文物建筑。

', 'HOTEL', '上海市黄浦区九曲桥路31号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (53, '外滩源 PARK Hyatt 行政江景房', 22, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/外滩源_PARK_Hyatt_行政江景房.jpg', 1650.0, 15, '入住外滩源PARK Hyatt行政江景房，每一天都像在欣赏一幅流动的城市画卷。

房间位于高层，窗外就是黄浦江与陆家嘴的壮阔景致——清晨江面薄雾轻笼，对岸摩天楼群若隐若现；傍晚夕阳为外滩历史建筑镀上金边，江上轮渡缓缓穿行。

除了全天候的江景视野，行政楼层还包含专属酒廊权益，每天下午可以享用现磨咖啡、精选茶饮与精致点心，透过全景玻璃窗静静看着城市天际线在眼前展开。

入夜后，你无需挤在观景台，在房内就能独享外滩灯光秀的私人场次，无论是商务出行中的片刻放松，还是与家人共度难忘时光，这里都能将外滩的历史韵味与现代活力融入你的日常停留。

', 'HOTEL', '上海市黄浦区中山东一路199号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (54, '广州塔璀璨套房', 27, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/广州塔璀璨套房.jpg', 1880.0, 8, '在广州塔107层428米高的璀璨套房中，珠江夜景如流动的星河在脚下铺展，全景落地窗将海心沙亚运公园的流线轮廓与珠江新城摩天楼群尽收眼底。

套房配备的恒温按摩浴缸紧邻窗边，采用防雾玻璃工艺，让你在浸泡放松时不错过窗外每一帧景致；倒入随房附赠的定制白兰香薰，清雅气息随水汽弥漫，与远处猎德大桥的车流光影交织成多维体验。

当整点时分塔身灯光秀亮起，七彩光束穿透夜色，与手中起泡酒交相辉映，这一站在城市之巅的瞬间，既适合纪念日庆祝，也为商务接待增添独特质感。

套餐含双人观景台通票及晚间专属通道，让你在静谧高空将转瞬即逝的感动化为可收藏的记忆。

', 'HOTEL', '广州市海珠区阅江西路222号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (55, '曾厝垵里弄美宿·露台房', 34, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/曾厝垵里弄美宿·露台房.jpg', 520.0, 20, '在曾厝垵文创村的红砖古厝群中，这间露台房既延续了闽南传统建筑的条石基座与红砖墙面，又巧妙融入了现代生活的便利。

朝西偏南的露台拥有开阔视野，清晨时海风轻抚过木饰面吧台，远处隐约传来渔船的声响；傍晚则能欣赏夕阳为村落屋顶镀上金边的景致。

我们为您准备了手冲咖啡体验，配备专业器具与两种精选咖啡豆，在驻店咖啡师的指导下，您可以在露台悠闲冲泡，伴着咖啡香感受渔村从古朴走向文艺的变迁。

这里不仅是旅途中的歇脚处，更让您真切体验一段慢节奏的闽南日常。

', 'HOTEL', '厦门市思明区曾厝垵西路45号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (56, '泉州开元寺香宿', 35, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/泉州开元寺香宿.jpg', 480.0, 18, '在泉州开元寺这座千年古刹的晨光中醒来，您将入住由闽南红砖古厝改造的禅意空间，推开木格窗便能望见唐代石塔的剪影。

每日清晨五点半，跟随僧侣完成限量的晨钟祈福仪式后，步行三分钟即可享用寺内斋堂为您准备的时令素斋，新鲜的笋菇与泉州面线糊正冒着热气。

午后可在保留燕尾脊、胭脂砖等传统元素的庭院里品茶，看阳光透过镂空砖雕在花砖地面游走，夜晚则有机会参与佛经抄写等特色活动。

住宿经过专业团队现代化改造，在保持传统风貌的同时配备了舒适的静音空调与卫浴系统，让您在千年古寺的宁静氛围中获得身心放松的独特体验。

', 'HOTEL', '泉州市鲤城区开元寺西侧巷3号');
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (57, '外滩摄影徒步体验', 22, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/外滩摄影徒步体验.jpg', 268.0, 300, '清晨六点半的外滩格外宁静，黄浦江面泛着初醒的波光，万国建筑群在晨雾中渐次清晰。

这段1.5公里的徒步拍摄将从苏州河口延伸至外白渡桥，由持证摄影师带领您探寻五个精心设计的取景角度。

在专业全画幅相机的记录下，您将学习如何捕捉晨光在百年建筑立面的流转，抓拍江鸥掠过海关钟楼的瞬间，以及运用构图技巧让画面充满故事感。

两小时的拍摄结束后，摄影师会为您精选十张照片进行RAW格式专业修图，无论是想提升旅行摄影水平，还是希望获得避开人流的独特体验，这些带着晨露气息的成片都会成为值得反复回味的视觉记忆。

', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (58, '厦门海岛骑行日票', 32, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/厦门海岛骑行日票.jpg', 199.0, 260, '这张厦门海岛骑行日票让你以更自在的方式探索鼓浪屿，电助力单车能轻松应对岛上的起伏坡道，让你在骑行中感受海风的轻抚。

我们精心设计的路线会带你穿过龙山洞的清凉，沿着海岸线寻找观景平台，从不同角度欣赏厦门城市轮廓与海面白帆。

随车配备的防晒包不仅装有防晒用品，还有详细路线图和应急联络卡，让环岛骑行既安心又惬意。

清晨或傍晚是最佳骑行时段，这时斜阳洒在老建筑上，偶尔飘来的钢琴声更添岛屿韵味。

无论是想避开人潮发现隐秘角落，还是悠闲地环岛漫游，这张日票都能带你深入体验这座无车小岛的独特节奏。

', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (59, '北京胡同深度文化游', 18, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/北京胡同深度文化游.jpg', 299.0, 180, '从天安门广场出发，这趟胡同文化游带你深入老北京的脉络。

乘坐复古三轮车缓缓穿行于青砖灰瓦之间，专业向导会为你讲述胡同里的生活故事与建筑历史，覆盖西交民巷至什刹海一带。

途中探访私密四合院，在传统院落里边品尝炸酱面、冰糖葫芦等地道小吃，边了解老北京人的居住习俗。

行程中还可能遇上非遗传承人展示剪纸或空竹等民俗技艺，让文化体验更生动。

每团人数有限，确保你能安静感受胡同的日常氛围，从宏大的天安门广场到温润的胡同深处，完整领略北京的双重魅力。

', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (60, '珠海海风帆船体验', 31, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/珠海海风帆船体验.jpg', 420.0, 120, '在珠海长隆海洋王国附近的海域，您可以登上12米长的法国博纳多帆船，开启两小时的航海体验。

专业教练会指导您掌握掌舵与调帆的技巧，当船体迎着海风划开波浪时，既能感受到操控帆船的乐趣，又能欣赏到远处主题公园的童话天际线。

同行的亲友可以倚在船舷边，偶尔还能遇见跃出水面的中华白海豚群。

随船配备的运动相机和无人机将全程记录您的航海时刻，最后会收到30张精选照片和航拍视频，这些影像不仅留存了扬帆的英姿，更让碧海蓝天的记忆变得触手可及。

建议穿着防晒衣物与防滑鞋，让这段亲海之旅既安全又尽兴。

', 'TRAVEL', NULL);
INSERT INTO "product" ("id", "name", "scenic_id", "cover_image", "price", "stock", "description", "type", "hotel_address") VALUES (61, '上海咖啡地图联名杯', NULL, 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/上海咖啡地图联名杯.jpg', 158.0, 450, '这款上海咖啡地图联名保温杯将城市探索与日常实用巧妙结合，让你在品味咖啡的同时也能深度感受上海的独特气息。

杯身印有精心绘制的咖啡地图，标注了散布于梧桐街区与弄堂深处的二十余家独立咖啡馆，从田子坊的文艺烘焙空间到愚园路的自家拼配小店，每一处都是值得驻足的城市风景。

采用304不锈钢真空隔热技术，无论是清晨的手冲咖啡还是午后的冰酿茶饮，都能在六小时内保持恰到好处的温度。

280毫升的轻巧杯身易于握持携带，磨砂防滑表面兼顾手感与耐用性，无论是外滩漫步还是武康路闲逛，都能随时为你提供温暖陪伴。

随杯附赠的详细地图不仅标注了各店的招牌饮品与营业时间，还贴心提示地铁换乘信息，助你轻松规划属于自己的咖啡之旅。

这只杯子不仅是日常饮具，更成为连接你与这座城市咖啡文化的温柔媒介，让每一次举杯都成为探索上海的新起点。

', 'TRAVEL', NULL);
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
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (17, '故宫博物院', '北京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%95%85%E5%AE%AB%E5%8D%9A%E7%89%A9%E9%99%A2.png', '故宫博物院位于北京中轴线的核心，这座自明永乐年间开始营建的宫殿群已历经近六百年的沧桑变迁，至今仍完整呈现着中国古代建筑艺术的精髓。

作为明清两代二十四位皇帝的居所与政务中心，它不仅是世界上现存规模最大、保存最完整的木质结构古建筑群，更被联合国教科文组织列入世界文化遗产名录。

穿过午门五凤楼的拱券门洞，太和殿广场豁然展现，三层汉白玉须弥座托举起八米高的台基，重檐庑殿顶的金銮宝殿巍然矗立。

殿内金砖铺地，蟠龙藻井悬垂，昔日皇帝登基与大婚的盛典皆在此举行。

沿中轴线向北，中和殿与保和殿依次呈现，两侧分布着文华殿、武英殿等政务区域，以及东西六宫的生活院落。

乾清宫内悬挂的"正大光明"匾额仍按原样陈列，养心殿保留着垂帘听政时期的布局，宁寿宫区的乾隆花园则巧妙融合了江南园林的造景手法。

每年秋季，神武门城楼的紫禁城建筑艺术展系统展示斗拱构件、琉璃瓦作等珍贵文物。

钟表馆设于奉先殿，陈列着十八世纪英国进贡的机械钟表；珍宝馆位于皇极殿区域，展出的金瓯永固杯、点翠凤冠等器物见证着明清工艺的卓越成就。

建议游客从午门进入后沿中轴线参观，最后登临神武门远眺景山，完整感受这座宫城前朝后市、左祖右社的营建智慧。

', '北京市东城区景山前街4号', 39.9163, 116.3972, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (18, '天安门广场', '北京', 'https://images.pexels.com/photos/68704/pexels-photo-68704.jpeg', '天安门广场坐落在北京城市中轴线的核心位置，南北延展880米，东西横跨500米，44万平方米的辽阔空间使其成为全球最大的城市广场。

青石板地面每日承载着国旗护卫队铿锵有力的步伐，晨光中的升旗仪式与暮色里的降旗仪式如同精准的时钟，见证着时光流转。

北侧的天安门城楼延续着明代九开间重檐歇山顶的规制，朱红柱廊与金水河上七座雕琢蟠龙纹样的汉白玉石桥相得益彰。

向南漫步，毛主席纪念堂的方形廊柱与人民英雄纪念碑的汉白玉浮雕形成时空对话，碑座八幅浮雕默默述说着近代历史的波澜壮阔。

节日期间，十万余盆花卉构筑的立体景观在灯光映衬下焕发别样生机，东侧国家博物馆的庄重轮廓与西侧人民大会堂的浅黄立面共同勾勒出沉稳的城市画卷。

若在平日清晨到访，不仅能避开熙攘人群，还能目睹晨光渐次点亮广场建筑群的细腻瞬间。

', '北京市东城区东长安街', 39.9033, 116.3915, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (19, '颐和园', '北京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E9%A2%90%E5%92%8C%E5%9B%AD.png', '漫步在颐和园中，昆明湖的波光与万寿山的绿意相映成趣，这座历经三百年的皇家园林以自然的山水格局承载着深厚的历史记忆。

沿着蜿蜒的长廊缓步前行，梁枋上数千幅彩绘细腻地描绘着民间故事与山水意境，湖面倒映出玉带桥的优雅弧线，佛香阁掩映于苍松翠柏间，俯瞰着整个园林的布局。

昔日慈禧太后泛舟的水道依然可通航，铜牛静守的湖畔垂柳轻摇，每一处太湖石的摆放都见证着从清漪园到颐和园的岁月流转。

园中建筑群完整保留了清代官式营造的特色，长廊彩绘系统涵盖了人物、山水等丰富题材，形成独特的艺术长廊，而昆明湖与万寿山的巧妙结合，不仅体现了传统园林的水陆比例原则，更通过引水借景的手法，实现了景观与水利功能的和谐统一。

', '北京市海淀区新建宫门路19号', 39.993, 116.2755, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (20, '圆明园遗址公园', '北京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%9C%86%E6%98%8E%E5%9B%AD%E9%81%97%E5%9D%80%E5%85%AC%E5%9B%AD.png', '圆明园遗址公园坐落于北京西北郊，这片静谧的土地曾被誉为万园之园，凝聚着中国古典园林艺术的精髓。

沿着蜿蜒小径漫步，四十景的宏大格局依稀可辨——西洋楼的断柱在暮色中投下斑驳斜影，大水法的残存石雕虽已风化，却依然勾勒出当年喷泉水幕交错的盛况。

中式庭院的曲径通幽与西式建筑的巴洛克曲线在此交融，石构件上的雕花纹样仍保留着十八世纪东西方对话的痕迹。

重建的鉴碧亭与涵秋馆重现了昔日的空间韵律，春日山桃缀满坡岸时，秋日银杏洒金于石阶，野花在残垣间摇曳，蝴蝶掠过石刻的葡萄纹样。

从东门步入复原的九州景区，能感受到皇家园林既恢弘又沉静的特质，每一处遗迹都在无声讲述着艺术与历史交织的过往。

', '北京市海淀区清华西路28号', 40.008, 116.3043, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (21, '八达岭长城', '北京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%85%AB%E8%BE%BE%E5%B2%AD%E9%95%BF%E5%9F%8E.png', '八达岭长城是明代长城中保存最为完好的段落之一，被联合国教科文组织列入世界文化遗产。

这段城墙沿着燕山山脊绵延伸展，登上北八楼敌台远望，城墙如游龙般穿行于起伏的峰峦之间。

清晨的阳光洒在青砖垛口上，石阶还带着未干的露水，从关城到好汉坡的每一级台阶都记录着无数旅人的足迹。

北四楼的好汉石是这段路程的标志，站在此处既能体会"不到长城非好汉"的意境，又能近距离观察完整的箭楼和烽火台建筑。

春秋两季的山景格外清晰，建议赶在首班接驳车前入园，避开人流高峰，独自感受晨曦中的古城墙。

攀登时需注意部分台阶坡度较陡，穿着防滑运动鞋并借助沿途扶手能确保安全行走。

', '北京市延庆区八达岭镇', 40.3653, 116.0203, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (22, '外滩', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%A4%96%E6%BB%A9.png', '沿着黄浦江畔漫步，这条约1.5公里的滨江步道串联起上海一个多世纪的城市记忆。

西侧52栋历史建筑静静矗立，从新古典主义线条的亚细亚大楼到装饰艺术风格的海关大楼，不同时期的建筑语言共同勾勒出"万国建筑博览"的轮廓；隔江相望的陆家嘴现代建筑群中，东方明珠、金茂大厦与上海中心在云端相映成趣。

清晨的江风里常有晨练者打着太极，午后的防汛墙前聚集着拍照的游客，游船在江面划出淡淡的水痕。

当暮色降临，两岸灯火渐次点亮，外白渡桥的钢铁骨架与对岸摩天楼的玻璃幕墙倒映在水中，仿佛进行着跨越百年的对话。

作为见证上海从开埠到开放历程的露天博物馆，外滩始终以它独特的城市剪影，记录着这座城市的变迁与传承。

', '上海市黄浦区中山东一路', 31.24, 121.49, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (23, '东方明珠', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%9C%E6%96%B9%E6%98%8E%E7%8F%A0.png', '矗立于陆家嘴黄浦江畔的东方明珠广播电视塔，以468米的高度串联起十一个错落有致的球体，从空中望去犹如一串被江风拂动的珍珠项链。

乘坐高速电梯抵达263米观光层，环形玻璃幕墙将外滩百年建筑群与陆家嘴现代天际线同时纳入视野，黄浦江的粼粼波光在脚下蜿蜒伸展。

继续下行至259米悬空廊道，站在透明地板上可见渡轮划开江面，陆家嘴环形天桥的车流如银河流动。

塔座内的城市历史陈列馆用微缩场景还原了老城厢风貌，从石库门里弄到外滩钟楼，见证着上海开埠以来的变迁轨迹。

当暮色浸染天际，球体渐次亮起柔光，与对岸万国建筑群的轮廓灯共同勾勒出这座城市独有的昼夜交替。

作为浦东开发开放的时代印记，这座电视塔始终是读懂上海过去与未来的立体坐标。

', '上海市浦东新区世纪大道1号', 31.2397, 121.4998, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (24, '豫园', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%B1%AB%E5%9B%AD.png', '坐落于上海老城厢的豫园，是江南园林艺术的典范。

它始建于明代嘉靖年间，占地三十余亩，至今仍完整保留着明清时期的建筑格局。

穿过蜿蜒的九曲桥进入园内，虚实相生的景致徐徐展开：漏窗巧妙引入城隍庙的市井烟火，嶙峋假山与潺潺瀑布又自然隔绝了外界喧嚣。

三穗堂、仰山堂、点春堂等四十余座古建筑临水而筑，玉玲珑石与龙墙砖雕的细节处处可见营造者的巧思。

虽身处现代都市中心，园内仍延续着茶艺展演、元宵灯会等传统习俗。

建议清晨开园时造访，既能避开人潮独享回廊间渐次铺展的晨光，也可在得月楼沏一壶龙井，感受四百年来未曾改变的江南生活韵律。

青石板上深浅交错的磨损痕迹，默默见证着从明清文人到今日游人的步履，让这座园林不仅是凝固的历史，更是持续生长的文化空间。

', '上海市黄浦区安仁街137号', 31.2271, 121.4926, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (25, '迪士尼乐园', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%BF%AA%E5%A3%AB%E5%B0%BC%E4%B9%90%E5%9B%AD.png', '坐落于上海浦东新区的迪士尼乐园占地约3.9平方公里，相当于546个标准足球场的大小。

园区以奇幻童话城堡为中心展开布局，这座高69米的建筑是全球迪士尼乐园中最高的城堡。

七大主题区域各具特色，米奇大街充满欢快氛围，奇想花园展现童话色彩，梦幻世界则带领游客进入经典故事场景。

创极速光轮过山车以每小时100公里的速度在发光轨道上穿梭，加勒比海盗项目通过创新驾乘系统重现电影中的冒险场景。

每天下午，由120位演员组成的巡游队伍沿主干道进行花车表演；夜晚的灯光秀以城堡为幕布，配合激光喷泉和烟花，形成直径300米的立体视觉画面。

园内绿化精心设计，栽种了2.4万棵乔木和100万株灌木，包括200棵来自黄山的百年古树。

18个主题餐厅和50家特色商店提供多样选择，其中米奇造型冰淇淋年销量达200万支。

翱翔·飞越地平线项目通过巨型球幕技术，让游客在6分钟内欣赏全球18处自然奇观，其升降系统可同时容纳90人。

自2016年开放以来，年均接待游客约1200万人次，七个小矮人矿山车在黄金周曾创下240分钟的排队记录。

园内处处可见精心设计的细节：排水盖上的米奇图案、路灯顶部的魔法水晶装饰，以及随处可见的迪士尼角色互动点，共同营造出这个充满魔法的体验空间。

', '上海市浦东新区川沙新镇', 31.144, 121.657, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (26, '田子坊', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E7%94%B0%E5%AD%90%E5%9D%8A.png', '漫步田子坊的窄巷，脚下是磨得发亮的青石板路，两侧灰砖拱门内藏着各具特色的创意空间。

这些建于上世纪三十年代的石库门建筑依然保持着原有的居住格局，晾衣杆从雕花铁窗探出，与墙角的绿植共同勾勒出市井生活的日常图景。

转角可能遇见陶艺师正在修整胚体，或是从半掩的木门后瞥见画师调试新作。

阳光透过梧桐树隙洒在斑驳的墙面上，咖啡香与颜料气息在巷弄间淡淡交织，让传统里弄在保留生活烟火气的同时，自然生长为承载当代艺术创作的独特空间。

', '上海市黄浦区泰康路248弄', 31.2082, 121.4663, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (27, '广州塔', '广州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%B9%BF%E5%B7%9E%E5%A1%94.png', '矗立于珠江南岸的广州塔以600米的高度成为中国第一高塔。

修长的塔身呈现出螺旋上升的流线形态，银白色外立面在晴日里映照着云絮与天光。

当暮色四合，千余盏LED灯渐次亮起，在塔体表面勾勒出不断变幻的光影脉络。

搭乘高速电梯抵达428米观景台，脚下全透明玻璃地板将城市景象完整呈现——珠江如一条波光粼粼的绸带蜿蜒穿过城区，错落的建筑群自脚下延伸至远方的天际线。

位于塔顶的旋转餐厅以每100分钟一周的节奏缓缓转动，游客在品尝地道粤式点心的过程中，可透过环形落地窗将羊城胜景徐徐尽收。

这座曾作为2010年亚运会开幕式背景的建筑，既是迎接八方来客的城市地标，也是感受广州现代脉搏的立体窗口。

', '广州市海珠区阅江西路222号', 23.1065, 113.324, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (28, '白云山', '广州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E7%99%BD%E4%BA%91%E5%B1%B1.png', '白云山素有羊城第一秀之称，是广州重要的城市绿肺，也是国家5A级旅游景区。

整座山脉由三十多座山峰连绵而成，主峰摩星岭海拔382米，登临其上可将广州城景尽收眼底。

清晨时分，山间常笼罩着薄雾，如轻纱般萦绕在翠绿林海之间，白云山因此得名。

这里保存着完整的南亚热带常绿阔叶林，近千种植物在此生长。

沿途可见明代山门、清代能仁寺与民国时期的碑林石刻，记录着不同年代的历史痕迹。

半山腰的云台花园四季花开不断，春杜鹃、夏荷、秋桂与冬茶梅交替绽放。

站在山顶观景台远眺，珠江如银练蜿蜒穿过城市，广州塔与东西塔勾勒出清晰的天际线。

傍晚夕阳为群山镶上金边，华灯初上时，山间鸟鸣与城市灯火相映成趣，展现出自然与都市和谐共生的独特景致。

', '广州市白云区白云大道', 23.1666, 113.293, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (29, '深圳世界之窗', '深圳', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%B7%B1%E5%9C%B3%E4%B8%96%E7%95%8C%E4%B9%8B%E7%AA%97.png', '深圳世界之窗坐落于深圳湾畔，是一座以世界文化微缩景观为主题的大型乐园。

园区占地四十八万平方米，按不同比例精心复刻了全球一百三十多处知名地标，从等大的埃菲尔铁塔到缩小的金字塔，让游客在漫步中感受多元文明的交融。

穿过日本庭园的竹篱，耳边响起威尼斯水城的船歌，每个区域都以细腻的景观设计呈现独特风貌。

作为国家5A级景区，这里配备多语种导览和互动设施，定期更新主题巡游和夜间演出，结合全息投影等技术增强体验。

园内设有环幕影院、游乐项目和餐饮区，全年开放并提供夜间照明，适合不同时段的游览，让人们在方寸之间领略世界文化的精华。

', '深圳市南山区深南大道9037号', 22.5333, 113.973, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (30, '锦绣中华民俗村', '深圳', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E9%94%A6%E7%BB%A3%E4%B8%AD%E5%8D%8E%E6%B0%91%E4%BF%97%E6%9D%91.png', '锦绣中华民俗村坐落于深圳南山区，是国内首个以中华文化为主题的大型景区。

园区由微缩景观与民俗村落两大部分组成，前者以精确比例还原了长城、故宫等八十余处标志性景观，漫步其间可一览中国南北的地貌特色与建筑精华；后者则汇聚了二十多个少数民族的村寨，每日由非遗传承人现场展示传统手工艺，游客不仅能观赏傣族舞、蒙古马头琴等民俗表演，还可亲手体验剪纸、银饰制作等互动项目。

傍晚时分，剧场会上演融合多媒体技术的民族服饰秀，通过华美服饰与音乐舞蹈呈现各民族的文化精髓。

建议乘坐地铁一号线至华侨城站直达，园区内设有各地风味餐饮及文化体验区，适合安排全天游览。

', '深圳市南山区华侨城', 22.5382, 113.981, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (31, '长隆海洋王国', '珠海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E9%95%BF%E9%9A%86%E6%B5%B7%E6%B4%8B%E7%8E%8B%E5%9B%BD.png', '长隆海洋王国位于珠海横琴新区，占地132公顷，是全球规模领先的海洋主题公园之一。

园区以极地探险和海洋生态为主题，分为八大区域，其中鲸鲨馆饲养着珍稀的鲸鲨，透过巨大的观景窗可以看到它们悠然游动的身影；企鹅馆则还原了南极的冰雪环境，成群的企鹅在飘雪中摇摆行走。

每天在中心湖上演的海洋奇观表演结合了水上飞人、无人机阵列和灯光投影，营造出立体的视觉效果。

亚洲首座水上过山车“冰山过山车”让游客在急速滑行时能近距离观察北极熊的生活场景。

作为吉尼斯认证的海洋馆，这里汇聚了众多珍稀海洋生物，让游客在游乐设施与生态展示之间感受海洋的丰富多样。

建议清晨先参观鲸鲨馆以避开人流高峰，下午观看动物行为展示，傍晚在横琴海畔欣赏融合灯光与水幕的夜间巡游。

园内还设有主题餐厅和纪念品商店，可以品尝造型别致的海洋主题餐点，或带走独家设计的企鹅玩偶作为纪念。

', '珠海市横琴新区富祥湾', 22.098, 113.533, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (32, '鼓浪屿', '厦门', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E9%BC%93%E6%B5%AA%E5%B1%BF.png', '鼓浪屿这座世界文化遗产岛屿静卧于厦门湾中，登岛便能感受到远离尘嚣的宁静——没有车马喧哗，只有海浪轻抚礁石的节奏与从古老窗棂流淌出的钢琴旋律。

作为19世纪中叶形成的国际社区，岛上留存着十三国领事馆旧址，红砖骑楼与欧式拱廊在榕树荫蔽下错落交织，斑驳墙面记录着中西文化碰撞的岁月痕迹。

这里每十条巷弄就有一架钢琴，周淑安等音乐家的故居依然飘荡着练习曲，让音乐成为岛屿的呼吸节拍。

登上日光岩可眺望厦门城市轮廓，当年郑成功练兵的石阶如今蜿蜒至观景平台，脚下是翡翠般的海湾与星罗棋布的洋楼。

菽庄花园巧妙借景大海，潮汐在亭廊间自成画卷，而八卦楼的赭色穹顶、海天堂构的彩色玻璃窗则诉说着建筑背后的百年故事。

建议从邮轮中心乘船抵达后，先在龙头路尝一碗鱼丸汤，再任选一条青石板小路漫行，晨昏时分流动的光影会让红砖墙与三角梅呈现出最动人的模样。

', '厦门市思明区鼓浪屿', 24.4458, 118.0703, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (33, '南普陀寺', '厦门', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8D%97%E6%99%AE%E9%99%80%E5%AF%BA.png', '南普陀寺静静倚靠在厦门五老峰的山脚下，这座千年古刹自唐代延续至今，始终是闽南信众心中的佛教圣地。

沿着山势铺展的红墙灰瓦建筑错落有致，与苍翠的山林融为一体。

每日晨光初现时，悠远的钟声便在山谷间流转，香客们踏着青石台阶拾级而上，缕缕青烟伴随着诵经声在殿宇间萦绕。

大雄宝殿内供奉的佛像宝相庄严，檐下精雕细琢的彩绘凝结着不同朝代的艺术印记，珍藏的明代铜钟与清代经书更是岁月沉淀的见证。

顺着寺院后方的石阶登临五老峰观景台，可见整片建筑群与厦门大学相邻而立，远处海面在日照下泛起细碎银光。

每逢传统佛诞节日，寺内便会举行庄严的法会，信众虔诚礼拜的身影与袅袅梵唱交织成独特的宗教图景。

古刹旁的素斋馆延续着悠久的素食传统，以闽南烹饪手法呈现食材本真滋味，让来访者既能领略古寺的宁静祥和，亦可体验清净淡雅的寺院饮食文化。

', '厦门市思明区思明南路515号', 24.436, 118.096, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (34, '曾厝垵文创村', '厦门', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%9B%BE%E5%8E%9D%E5%9E%B5%E6%96%87%E5%88%9B%E6%9D%91.png', '曾厝垵坐落在厦门环岛路旁，由昔日渔村蜕变为一处融合传统与现代的文创聚落。

红砖古厝的屋檐下延伸出创意墙绘，老建筑里藏着独立设计店、手作工坊与飘着咖啡香的小馆。

巷弄间，百年宗祠静立一旁，转角可能遇见色彩鲜明的街头涂鸦；繁茂的老榕树下，常有人弹着吉他，茶香随风飘散。

周末的创意市集汇聚了手作饰品、原创画作和闽南特色小吃，吸引游人驻足。

这里既留存着渔村的宁静海风，又跃动着年轻的艺术活力，成为感受厦门本土文化与当代创意的独特角落。

', '厦门市思明区曾厝垵', 24.4385, 118.131, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (35, '开元寺', '泉州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%BC%80%E5%85%83%E5%AF%BA.png', '在泉州古城的中心地带，开元寺已静静伫立了千年时光。

这座福建省规模最大的佛教寺院始建于唐垂拱二年，原址曾是一片茂盛的桑园。

据传因园中桑树绽放莲花，刺史黄守恭遂舍园建寺，初名莲花寺，后于开元年间诏改今名。

穿过古朴的山门，五进深的院落徐徐展开，占地七万八千平方米的寺院严格遵循唐宋时期的建筑规制，天王殿、大雄宝殿、甘露戒坛、藏经阁等主体建筑沿中轴线次第排列。

大雄宝殿内二十四尊迦陵频伽斗拱独具匠心，这些妙音鸟手持各式乐器，衣袂轻扬，完美融合了佛教艺术与闽南工匠的智慧。

月台束腰处镶嵌的七十二幅狮身人面青石浮雕尤为珍贵，带着古埃及风情的石刻默默见证着宋元时期泉州作为海上丝绸之路起点的辉煌过往。

东西对峙的镇国塔与仁寿塔是我国现存最高的孪生石塔，八角五层的楼阁式结构在晨昏光影间勾勒出庄严轮廓。

当暮色浸染双塔檐角，晚钟声穿过古榕气根，这座千年古刹依然延续着闽南地区最深厚的佛教文化传承。

', '泉州市鲤城区西街', 24.9122, 118.588, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (36, '清源山', '泉州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%B8%85%E6%BA%90%E5%B1%B1.png', '清源山作为闽南文化的标志性山脉，历经千年沉淀，花岗岩山体间散布着众多石雕遗迹，步道两侧可见唐代佛教摩崖造像的庄严轮廓与宋元时期道教题刻的飘逸笔迹。

山泉自岩脉缓缓渗出，在麒麟谷汇聚成常年流淌的溪涧，经专业检测证实富含对人体有益的矿物质元素。

攀登至海拔498米的主峰时，整座泉州城的屋宇街巷与晋江入海口的粼粼波光尽收眼底。

沿途历代文人墨客的诗词碑刻与弘一法师舍利塔静立林间，若选择清晨沿石阶缓步而上，既能避开拥挤人潮，又可聆听泠泠泉声与远处古寺钟鸣在山谷间交织回荡的静谧韵律。

', '泉州市丰泽区清源山', 24.946, 118.607, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (37, '西湖', '杭州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%A5%BF%E6%B9%96.png', '西湖作为世界文化遗产，承载着千年来人与自然和谐共生的智慧。

清晨的苏堤上，晨练的市民与初阳一同苏醒，垂柳的嫩芽在三月悄然萌发，成为杭州春天最早的讯息。

手摇船缓缓穿过跨虹桥，船夫会指着湖心三潭印月的石塔，讲述它们与雷峰塔遥相呼应的故事。

六月的湖面铺展着连绵的荷花，粉白花瓣在微风中轻颤，与龙井茶山飘来的炒茶香气交织成独特的江南韵味。

这里不仅是马可·波罗笔下赞誉的华美之城，更是持续上演的生活剧场——老杭州人保留着环湖漫步的传统，茶农依旧按古法焙制新茶，这种鲜活的生活气息让西湖始终保持着跨越时空的生命力。

椭圆形的湖面周长约15公里，保存着自唐宋延续的山水格局，两条古堤、三座人工岛与四组古塔共同构成了这幅流动的文化长卷。

', '杭州市西湖区西湖风景区', 30.2431, 120.15, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (38, '灵隐寺', '杭州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E7%81%B5%E9%9A%90%E5%AF%BA.png', '灵隐寺静立于杭州西湖西北的飞来峰麓，东晋咸和元年始建的殿宇已在此绵延近一千七百年。

作为禅宗重要古刹，整座寺院依山取势，青瓦黄墙的殿阁在古木掩映间错落层叠，春日玉兰与秋日银杏为庭院点染时序流转的痕迹。

飞来峰岩壁上的五百罗汉造像静默伫立，青苔覆盖的石刻凝视着往来香客的身影。

大雄宝殿内24.8米的释迦牟尼坐像由二十六块香樟木雕成，当晨光穿过镂花木窗映照金身，檀香与诵经声便在梁柱间缓缓流淌。

从天王殿至华严殿的台阶渐次升高，唐代遗留的建筑规制让人在拾级时感受从尘世到禅境的过渡。

宋代契嵩禅师曾在此弘法著述，康熙御笔"云林禅寺"的匾额依然高悬山门。

寺内珍藏的贝叶经与明代金刚经刻本见证着千年法脉，每年腊八节延续的施粥传统更显佛门慈悲。

每日清晨六时，早课钟声惊起林间飞鸟，在九点半迎客前营造出独特的宁静——这座距市区仅二十分钟车程的古刹，始终为都市人保留着一处让心灵栖息的隐逸空间。

', '杭州市西湖区灵隐路法云弄1号', 30.24, 120.1167, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (39, '宋城景区', '杭州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%AE%8B%E5%9F%8E%E6%99%AF%E5%8C%BA.png', '宋城景区以北宋都城汴京为蓝本，完整重现了千年前的市井格局。

漫步在纵横交错的青石板路上，两侧是飞檐斗拱的仿古建筑，茶坊酒肆的布幌在檐角轻摇。

每日固定时段，街头会有耍猴、皮影戏等传统技艺表演，身着交领襦裙的工作人员提着竹篮穿行市集，与问路的游客自然交谈。

园区核心的《宋城千古情》实景演出通过水幕投影与威亚技术，再现了南宋宫廷的庆典场面。

游客可在茶寮体验宋代点茶技法，在投壶场尝试古人宴饮游戏，还能在仿汴京街市的食铺品尝现场制作的定胜糕与慢火东坡肉。

这里从建筑形制到器皿纹样均经过文史考证，既保留了严谨的历史肌理，又通过沉浸式互动让现代人触摸到宋代市井的生活温度。

', '杭州市西湖区之江路148号', 30.1873, 120.135, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (40, '天一阁', '宁波', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%A4%A9%E4%B8%80%E9%98%81.png', '漫步在宁波月湖畔的天一阁，这座始建于明代嘉靖年间的私人藏书楼，至今仍保持着中国现存最早的私家藏书建筑纪录。

青砖砌筑的楼阁错落有致，七万余卷古籍在透窗而入的柔和光线下泛着温润色泽。

主体建筑宝书楼暗合"天一生水"的营造理念，杉木构架与硬山式屋顶展现出古人藏书防火的智慧。

穿过月洞门可见东园的曲径回廊，江南园林的玲珑景致与藏书楼相映成趣。

这里延续着"代不分书，书不出阁"的祖训，三十余万卷文献在恒温恒湿的现代库房中得以妥善传承，其中明代地方志与科举录尤具特色。

作为持续运转四百余年的文化空间，它不仅记录着典籍保护的匠心，更见证着历代文人尊崇文脉的执着坚守。

', '宁波市海曙区天一街', 29.8781, 121.548, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (41, '雁荡山', '温州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E9%9B%81%E8%8D%A1%E5%B1%B1.png', '雁荡山静卧于浙江温州，这片联合国教科文组织认证的世界地质公园保留着白垩纪火山活动的印记。

历经一亿两千万年的地质雕琢，山体形成了错落的峰林与幽深的峡谷，一百零七座奇峰间点缀着二十九道飞瀑和四十六处古洞。

当夜幕降临，灵峰的轮廓在月光中悄然变幻，时而如展翅雄鹰，时而若相依的恋人。

三折瀑的水流沿着花岗岩崖壁分三级跃落，晴日里常有彩虹横跨水雾之间；大龙湫瀑布以一百九十七米的单级落差著称，丰水期似银龙俯冲深潭，枯水期则化作朦胧水烟。

方洞景区排列着规整的六棱石柱，这些火山岩柱状节理如同大地书写的自然密码。

宋代遗存的能仁寺中，铸于元祐七年的铁镬静立院中，诉说着千年梵音不绝。

沿着徐霞客当年踏勘的古道行走，原始森林掩映着蜿蜒石阶，春日可见杜鹃漫山铺展，雁荡山特有的冬青在岩缝间顽强生长。

每年四月至十月适宜探访，首日可循灵峰至三折瀑的线路，次日深入大龙湫与方洞区域。

山区气候随海拔变化显著，八百米以上地带夏季比市区凉爽许多，建议备好防风衣物与防滑登山鞋。

', '温州市乐清市雁荡镇', 28.3866, 121.168, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (42, '中山陵', '南京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%AD%E5%B1%B1%E9%99%B5.png', '中山陵静卧在南京紫金山南麓的苍翠林海间，作为孙中山先生的安息之地，这座近代纪念建筑群以独特的空间叙事向访客述说历史。

缓步攀登三百九十二级石阶，两侧青松列队相迎，蓝瓦白墙的陵寝建筑在层层抬升中渐次展开轮廓。

设计师吕彦直巧妙运用警钟形制，将"革命尚未成功"的醒世恒言凝固在建筑语言中。

穿过镌刻"博爱"的牌坊与碑亭，墓室内汉白玉坐像垂目端详，这位终结千年帝制的先驱便长眠于塑像之下。

建筑群依山取势形成的钟形布局，与八万平方米纪念植物园构成天地对话的有机整体。

清明时节，紫薇的淡紫与银杏的金黄为庄严的朝圣之路注入自然韵律。

立于祭堂平台，"天下为公"的墨迹与金陵城郭尽收眼底，此处既是爱国主义教育的现场课堂，也是解读近代中国的重要密码。

若择晨曦初露或暮色四合时来访，更能感受时间在这片建筑群中沉淀的独特质感。

', '南京市玄武区紫金山南麓', 32.064, 118.85, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (43, '夫子庙秦淮河', '南京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%A4%AB%E5%AD%90%E5%BA%99%E7%A7%A6%E6%B7%AE%E6%B2%B3.png', '夫子庙秦淮河是南京历史悠久的城市中心，这里以夫子庙古建筑群与秦淮河风光共同构成了独特的文化商业景观。

白天，你可以漫步在青石铺就的街巷间，探访江南贡院、乌衣巷等历史遗迹，感受古代科举文化的深厚底蕴。

沿河而建的明清风格建筑保持着传统风貌，商铺里既有地道的老字号小吃，也有精致的文创产品。

当暮色降临，河面上的画舫陆续点亮灯笼，柔和的光影倒映在水波中，与岸边的灯火交织成一片温馨景象。

这里既是游客了解南京历史的重要窗口，也是市民日常休闲、品尝美食的活跃场所，古今交融的氛围让每位到访者都能找到自己的体验方式。

', '南京市秦淮区秦淮河畔', 32.022, 118.8, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (44, '拙政园', '苏州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%8B%99%E6%94%BF%E5%9B%AD.png', '拙政园作为江南园林的杰出代表，以其精妙的造园艺术被列入世界文化遗产。

这座始建于明代的古典园林以水景为核心，亭台、廊榭依水而筑，营造出浑然天成的自然意境。

沿着蜿蜒的曲径漫步，梧竹幽居的翠影与远香堂的敞朗相继呈现，透过精巧的漏窗可见北寺塔倩影，月洞门中映着粼粼波光，这些借景手法让有限空间延展出无限意趣。

四季在此各有其美：春日海棠纷飞，夏日荷香满亭，秋日枫染亭台，冬日梅影疏斜。

清晨时分园中最是清幽，薄雾中的倒影与飞檐构成动人画卷。

留意各处匾额题字，能感受到古代园主寄情山水的心境。

这座占地七十八亩的园林不仅是造园艺术的典范，更凝聚着中国文人五百年的审美追求。

若想深入了解其假山堆叠之妙与水系布局之巧，可预约专业讲解，细细品味这座园林的独特韵味。

', '苏州市姑苏区东北街178号', 31.329, 120.633, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (45, '虎丘', '苏州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%99%8E%E4%B8%98.png', '虎丘坐落在苏州城西北方向，这座仅34米高的小山丘承载着两千五百年的历史沉淀。

相传春秋时期吴王阖闾安葬于此，三日后有白虎盘踞墓上，因而得名。

沿着青石板路徐徐上行，两旁古树参天，斑驳的树影轻抚着岁月打磨的石阶。

云岩寺塔是最引人注目的建筑，这座始建于五代后周的古塔微微倾斜，青砖砌成的塔身每层檐角都悬挂着铜铃，风起时传来清越的声响。

站在塔下仰望，能清晰感受到塔身历经千年仍巍然屹立的坚韧。

剑池是一汪被陡峭石壁环抱的碧水，传说中吴王的三千宝剑深藏于此，池畔岩壁镌刻着历代文人题字，王羲之"剑池"二字尤为醒目。

清晨时分，池面薄雾与岩壁青苔相映成趣。

千人石是一片暗红色的宽阔石坪，相传东晋高僧生公曾在此讲经，留下"顽石点头"的典故。

立于石上远眺，苏州古城白墙黛瓦的民居与纵横水巷尽收眼底。

建议工作日上午前往游览，从山门至山顶步行约需一小时，春秋时节园内古银杏满树金黄，丹桂清香浮动，更显意境悠远。

', '苏州市姑苏区虎丘山门内8号', 31.337, 120.598, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (46, '周庄古镇', '苏州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%91%A8%E5%BA%84%E5%8F%A4%E9%95%87.png', '周庄古镇被誉为中国第一水乡，清晨的薄雾笼罩着蜿蜒河道，初升的阳光将两岸白墙黛瓦的倒影铺在水面。

十四座古石桥静静连接着青石板铺就的街巷，沿河而建的明清民居保留着原始格局，木雕花窗后飘出老茶馆的袅袅茶香。

双桥作为镇中标志，因画家陈逸飞的《故乡的回忆》而闻名于世，青石桥拱与流水相映成趣。

乌篷船缓缓穿桥而过，船娘的吴侬软语伴着橹声荡漾，傍晚时分临水灯笼渐次亮起，勾勒出江南水乡特有的生活图景。

这里不仅完整保存着元明清三代的水乡肌理，更以每平方公里四公里的河道密度，展现着“井”字形水系与街巷交织的独特格局，成为研究江南聚落形态的活态标本。

', '苏州市昆山市周庄镇', 31.118, 120.858, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (47, '宽窄巷子', '成都', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%AE%BD%E7%AA%84%E5%B7%B7%E5%AD%90.png', '宽窄巷子由三条平行的清代老街构成，是成都现存为数不多完整保留清代城市肌理的历史街区。

青砖黛瓦的院落间散布着三十余处老茶馆，竹椅木桌沿街摆放，盖碗茶的清香与龙门阵的谈笑声从清晨延续到黄昏。

在这里，游客可以观赏传统掏耳朵手艺，聆听川剧票友在茶馆戏台上的即兴唱段，转角处常能遇见专注制作糖画的老师傅。

这些鲜活的市井生活场景，让宽窄巷子超越了单纯的观光地，成为体验成都茶文化与慢生活的立体窗口。

窄巷的幽静与宽巷的热闹形成微妙对比，青石板路两侧的百年梧桐在春夏投下斑驳树影，秋冬时节则与灰砖墙共同勾勒出水墨画般的意境。

虽然如今巷内增添了不少创意店铺和精致餐厅，但每天下午仍能看到老茶客摇着蒲扇在百年茶馆门前对弈，延续着三百年来未曾中断的市井烟火。

', '成都市青羊区长顺上街', 30.6667, 104.0667, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (48, '春熙路', '成都', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%98%A5%E7%86%99%E8%B7%AF.png', '春熙路作为成都的商业核心区域，历经百年沉淀，如今依然焕发着蓬勃生机。

青石铺就的街道两侧错落着国际精品店与川味小吃铺，转角处老字号抄手店的热气与时尚买手店的冷峻灯光形成有趣对比。

当暮色渐浓，仿古檐角下亮起的霓虹与商场玻璃幕墙交相辉映，游客们端着冰粉在爬墙熊猫雕塑前留影，本地人则坐在梧桐树下的茶馆里悠闲地打着纸牌。

街头艺人用吉他弹唱着方言民谣，歌声混着火锅香味在街巷间流转。

从清晨第一笼包子出屉到深夜最后一家酒吧打烊，这条街始终保持着流动的韵律，既延续着老成都人"摆龙门阵"的生活传统，又承载着现代都市的时尚脉动。

', '成都市锦江区春熙路商圈', 30.657, 104.08, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (49, '乐山大佛', '乐山', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B9%90%E5%B1%B1%E5%A4%A7%E4%BD%9B.png', '在岷江、青衣江与大渡河交汇的凌云山崖壁上，唐代工匠依山取势，凿刻出通高71米的乐山大佛。

整尊弥勒坐像历经三代匠人九十余年精心雕琢，相当于二十层楼的高度，当游船行至江心，佛像全貌渐次展开：整齐的螺髻、垂肩的双耳与抚膝的双手呈现出庄严法相，足下莲台承载着千年风雨。

尤为精妙的是隐藏在衣褶与耳后的排水暗渠，使这座石刻在潮湿多雨的蜀地依然轮廓清晰。

晨雾弥漫时，佛像仿佛凌空端坐；晴日可见青灰色岩体倒映江中，斑驳苔痕间时有飞鸟栖落佛肩。

作为现存最大的古代摩崖造像，它既展现了唐代佛教造像的巅峰技艺，也默默见证着三江航道的人间烟火。

建议于景区开放时段前往，乘船可览全景，徒步九曲栈道则能细观佛足石刻的岁月痕迹。

', '乐山市市中区凌云路', 29.552, 103.772, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (50, '峨眉山', '乐山', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%B3%A8%E7%9C%89%E5%B1%B1.png', '峨眉山坐落于四川盆地西南边缘，主峰万佛顶海拔3099米，是中国四大佛教名山之首，自古便是普贤菩萨的道场。

山中现存六十二座寺庙，其中八处被列为国家级文物保护单位，沿着三条主要朝圣路线分布。

从低山区的报国寺到清音阁，中山区的洪椿坪至洗象池，再到高山区的接引殿直达金顶，沿途可见青石阶蜿蜒、古刹错落。

金顶之上，十方普贤圣像巍然矗立，高达48米，由锻铜工艺铸成，重达660吨。

山中常年云雾缭绕，晨钟暮鼓间，香客往来，梵音与自然之声相融。

无论徒步朝圣或乘坐索道，这里全年大部分时间适宜探访，让人在行走中感受佛教文化的深厚与山林的宁静。

', '四川省峨眉山市', 29.589, 103.332, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (51, '北川羌城旅游区', '绵阳', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8C%97%E5%B7%9D%E7%BE%8C%E5%9F%8E%E6%97%85%E6%B8%B8%E5%8C%BA.png', '北川羌城旅游区位于四川盆地西北部，是羌族世代居住的聚落，也是一处延续着古老生活方式的民族文化空间。

沿着青石板路缓步前行，依山而建的羌族碉楼错落有致，片石与黄泥砌筑的墙体在岁月侵蚀中依然保持着沉稳的轮廓。

屋檐下常有老人手持彩线刺绣，针脚间流淌着记录祖先迁徙的纹样。

午后广场常飘起羌笛声，《羌山古歌》苍凉的曲调仿佛将三千年的民族记忆揉进了山风里。

游客可以随着羊皮鼓的节奏跳一段沙朗舞，若恰逢农历十月初一的羌年庆典，还能目睹释比头戴金丝猴皮帽吟唱古羌语史诗的庄严场景。

非遗工坊里提供羌绣制作与古法咂酒酿造的体验，由石砌民居改造的展厅陈列着云云鞋、银饰与羊皮鼓，这些器物以无声的形态诉说着没有文字的民族如何用纹样传承历史。

站在观景台俯瞰，岷江支流蜿蜒穿过碉楼群，远山云雾与民居构成一幅淡雅的山居图。

这里既无喧闹的商肆，也无刻意的表演，只有口弦琴即兴弹唱的古老歌谣与火塘边弥漫的咂酒香，让人感受到云朵上的民族最真实的生活气息。

', '绵阳市北川县', 31.791, 104.468, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (52, '大理古城', '大理', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%A4%A7%E7%90%86%E5%8F%A4%E5%9F%8E.png', '大理古城静静依偎在苍山洱海之间，青石板铺就的街巷蜿蜒伸展，两侧是白族传统民居，灰瓦白墙与精雕细琢的照壁相映成趣。

阳光洒在石板路上，门楣彩绘无声地诉说着白族的本主信仰与古老传说。

每逢农历初二和十六，古城的市集便热闹起来，空气中飘散着乳扇的醇厚奶香，复兴路旁的手工艺人专注地敲打银器，发出清脆的声响。

登上五华楼极目远望，层层叠叠的院落向着洱海方向铺展，飞檐上的瓦猫默默伫立，仿佛守护着这座千年城池的宁静。

白族的三道茶不仅是待客之礼，更蕴含着人生先苦后甜再回味的深意；扎染作坊里，蓝白交织的图案记录着自然草木与手工技艺的对话。

从清晨在床单厂艺术区眺望苍山缭绕的云雾，到傍晚沿着人民路漫步，小酒馆里传来舒缓的民谣，大理古城以其从容不迫的节奏，让每位旅人都能在此寻得属于自己的一方天地。

', '大理市大理古城内', 25.698, 100.157, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (53, '洱海', '大理', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%B4%B1%E6%B5%B7.png', '洱海作为大理风花雪月的具象载体，将下关风、上关花、苍山雪与洱海月融汇成白族文化中绵延千年的诗意符号。

晨光初现时，湖面泛起薄雾，如细纱般轻柔笼罩着水面，倒映出苍山十九峰连绵的剪影，早起的渔人驾着传统猪槽船划过粼粼波纹，在静谧中开始一天的劳作。

午后的湖面被阳光染成湛蓝，成群红嘴鸥时而掠过水面，时而盘旋于岸边的垂柳之间，与湖岸摇曳的柳枝构成动静相宜的景致。

当暮色渐浓，夕阳余晖隐没在苍山脊线之后，皎洁的月轮便从东方缓缓升起，在平静的湖面上投下细碎银光，仿佛铺就一条通往梦境的水上小径。

这片水域不仅是自然景观，更承载着白族人的生活记忆——每逢月圆之夜，仍能看到当地老人面向洱海明月虔诚祈福，让古老的传统在湖光山色间生生不息。

', '大理市洱海生态区', 25.699, 100.23, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (54, '丽江古城', '丽江', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%BD%E6%B1%9F%E5%8F%A4%E5%9F%8E.png', '丽江古城始建于宋末元初，至今已有八百余年历史，这片占地七平方公里的古老城区以四方街为中心，蛛网般的街巷向外延展，将纳西族的生活脉络完整保留下来。

清晨时分，阳光掠过青石板路，映照在三坊一照壁的民居院落之间，墙上的东巴彩绘静静诉说着古老文字的秘密。

穿城而过的玉河水系串联起市井生活的烟火气，从四方街的喧闹市集到科贡坊的沉静书卷，处处可见纳西文化的印记。

夜幕降临后，传统飞檐下的灯笼与酒吧街的灯火在星空下交织，东巴文化研究院里仍保存着八千余卷经书，纳西古乐的旋律还在古老院落中回响。

这座活着的古城既延续着纳西族的文化根脉，又在时代变迁中保持着独特的生命力。

', '丽江市古城区', 26.872, 100.238, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (55, '玉龙雪山', '丽江', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E7%8E%89%E9%BE%99%E9%9B%AA%E5%B1%B1.png', '玉龙雪山静卧于云南丽江境内，作为北半球纬度最低的雪山群，十三座雪峰如同巨龙脊背般绵延不绝。

主峰扇子陡海拔5596米，终年积雪的银白山巅在亚热带常绿阔叶林的环抱中显得尤为独特。

这座雪山与城市保持着罕见的亲近距离，站在丽江古城便能望见云雾轻抚的山体轮廓。

冰川公园里，现代冰川在日照下折射出淡蓝色的光泽，山体垂直分布着从干热河谷到高山冻原的七个完整自然带。

沿着山势行进时，能清晰看到金沙江峡谷深邃的切割痕迹，纳西人世世代代将这座圣山尊为"三朵神"的化身，使得自然奇观与人文信仰在此完美交融。

', '丽江市玉龙纳西族自治县', 27.098, 100.177, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (56, '西双版纳热带雨林', '西双版纳', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%A5%BF%E5%8F%8C%E7%89%88%E7%BA%B3%E7%83%AD%E5%B8%A6%E9%9B%A8%E6%9E%97.png', '西双版纳热带雨林延伸在云南省最南端的土地上，作为中国大陆唯一完整留存的热带雨林生态系统，这里常年浸润在21℃的温和气候与80%以上的湿润空气中。

林间蕴藏着全国四分之一的植物与三分之一的野生动物，当你行走其间，六十米高的望天树撑开苍穹，板状根如天然浮雕般延展，藤蔓与附生植物在枝杈间交织成悬空的绿网。

晨雾未散时，长臂猿的鸣叫回荡于山谷，溪畔可见孔雀雉轻盈踱步，松软泥土上偶遇野生亚洲象群踏过的痕迹。

从低地河谷雨林到云雾缭绕的山地苔藓林，这片北回归线上的绿洲完整保存着垂直分布的植被带谱，如同一部鲜活的热带生态百科全书。

世代居住于此的傣族人以竹桥连通溪涧，凭古老智慧辨识草木药用，他们的生活与雨林的脉动始终相融，让这片土地始终保持着原始而平衡的呼吸。

', '景洪市橄榄坝', 21.995, 100.803, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (57, '天涯海角', '三亚', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%A4%A9%E6%B6%AF%E6%B5%B7%E8%A7%92.png', '天涯海角坐落于三亚西郊的马岭山下，绵延两公里的海岸线上散布着历经海浪雕琢的花岗岩礁石。

刻有“天涯”与“海角”字样的巨石矗立于潮汐之间，既是地理方位的标志，也承载着人们对于永恒情感的寄托。

清晨的薄雾中，礁石群轮廓渐显，退潮后的沙滩露出细腻纹理；傍晚时分，落日余晖为海面铺上金辉，归航的渔船在波光中摇曳。

相传古时恋人常至此许下誓言，如今蜿蜒的木栈道上，仍可见新婚夫妇在奇石前留影，老人们在椰林荫蔽处漫步闲谈。

这里既保留着自然塑造的礁石景观，也延续着代代相传的人文记忆，潮起潮落间，海浪始终轻抚着这段写满故事的岸线。

', '三亚市天涯区天涯海角景区', 18.305, 109.284, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (58, '亚龙湾热带天堂森林公园', '三亚', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%BA%9A%E9%BE%99%E6%B9%BE%E7%83%AD%E5%B8%A6%E5%A4%A9%E5%A0%82%E6%A3%AE%E6%9E%97%E5%85%AC%E5%9B%AD.png', '亚龙湾热带天堂森林公园坐落在三亚东南部，青山环抱之中，眼前是开阔的碧蓝海湾。

这座公园因电影《非诚勿扰Ⅱ》中多个场景的拍摄而为人熟知。

步入园区，茂密的热带雨林扑面而来，沿着起伏的木栈道缓缓前行，两旁是高低错落的热带植物，偶尔能瞥见猕猴轻巧地跃过树梢。

登上山顶观景台，整片亚龙湾在脚下铺展开来——海水由近及远渐次变深，雪白的沙滩弧线优美，与层叠的山林共同构成宁静而开阔的景致。

无论是想沉浸于自然气息，还是希望记录下热带独特的风光，这里都能让人不虚此行。

', '三亚市吉阳区亚龙湾', 18.2345, 109.6167, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (59, '蜈支洲岛', '三亚', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%9C%88%E6%94%AF%E6%B4%B2%E5%B2%9B.png', '蜈支洲岛位于海南三亚以北的海面上，因其心形轮廓与清澈水质常被称作中国的马尔代夫。

岛屿被通透的海洋环绕，水下能见度可达6至27米，是理想的潜水区域。

站在细软沙滩上，可以清晰看到海水从近岸的浅绿色逐渐过渡到远处的深蓝色。

潜入水中，形态丰富的珊瑚群构筑成海底生态园，各色热带鱼穿梭其间。

岛上设有不同难度的潜水点，无论初次体验还是持证潜水者都能找到合适区域。

除海洋活动外，岛上保留着成片的原始植被，椰林与天然礁石相互映衬。

作为国家5A级景区，这里在完善旅游服务的同时维护着自然生态平衡。

黎明时分，朝阳从海平面缓缓升起，为岛屿披上金色光芒；日落时刻，洒满霞光的情人桥成为游客钟爱的留影地点。

', '三亚市海棠湾镇', 18.3231, 109.769, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (60, '海口骑楼老街', '海口', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%B5%B7%E5%8F%A3%E9%AA%91%E6%A5%BC%E8%80%81%E8%A1%97.png', '海口骑楼老街是这座城市最具代表性的历史街区，漫步其间，仿佛走入一幅流动的时光画卷。

这些始建于二十世纪初的建筑群，由南洋归侨引入，沿中山路与得胜沙路延伸，形成连绵的拱廊通道，为行人遮挡着热带特有的烈日与阵雨。

仔细观察那些二至四层的楼宇立面，欧洲巴洛克风格的雕花与岭南传统骑楼结构巧妙融合，山花装饰与百叶窗在斑驳色彩中静静诉说着往昔商埠的繁忙景象。

沿街老字号茶楼里飘散着老爸茶的醇香，底层店铺仍保留着钟表修理和裁缝生意，而上层彩色墙面与雕花栏杆间晾晒的衣物、摆放的绿植，则延续着"下店上宅"的日常烟火气。

作为海南规模最大、保存最完整的骑楼建筑群，这里最适合在晨光初现或夕阳西下时探访，既能感受清凉的海风，又能在廊下遇见当地居民品茶闲聊的生动场景。

', '海口市龙华区中山路', 20.035, 110.349, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (61, '五指山风景区', '五指山', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%BA%94%E6%8C%87%E5%B1%B1%E9%A3%8E%E6%99%AF%E5%8C%BA.png', '五指山矗立在海南岛中部腹地，主峰海拔1867米，是整座岛屿的最高点。

茂密的热带雨林几乎覆盖了整片山区，行走其间，浓荫蔽日，耳边不时传来长臂猿悠长的啼鸣，斑斓的蝴蝶在盘根错节的绞杀榕丛中翩跹起舞。

雨季来临时常有云雾缠绕峰峦，若在晴朗天气登临山顶，能将海南岛起伏的地貌尽收眼底。

深山处散布着黎族村落，偶尔会遇到背着竹篓采药的村民。

这片原始森林不仅是重要的生态保护区与科研基地，也是众多登山爱好者向往的徒步路线。

', '五指山市五指山景区', 18.775, 109.516, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (62, '什刹海历史文化风景区', '北京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%BB%80%E5%88%B9%E6%B5%B7%E5%8E%86%E5%8F%B2%E6%96%87%E5%8C%96%E9%A3%8E%E6%99%AF%E5%8C%BA.png', '什刹海历史文化风景区静静卧在北京老城中心，由前海、后海和西海三片相连的水域构成，是城区内保存最完整的一片水景。

这里留存着元代漕运码头的痕迹，也延续着胡同与市井生活的脉络。

沿着湖岸走，垂柳轻点水面，银锭桥连接两岸，鼓楼的轮廓在波光中微微荡漾。

青砖灰瓦的四合院藏在纵横交错的胡同里，石榴树偶尔从墙头探出枝叶。

清晨常有老人提着鸟笼沿湖散步，午后的胡同回荡着自行车铃声，待到傍晚，酒吧街的灯笼一盏盏亮起，木船桨声轻轻拨动着夜色。

你可以坐三轮车穿行胡同，听车夫聊聊旧事，去老字号尝一口地道小吃，或坐在湖边看野鸭游过，感受这座古城七百年来绵延不绝的呼吸。

', '北京市西城区地安门外大街什刹海沿线', 39.9406, 116.3809, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (63, '奥林匹克公园', '北京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%A5%A5%E6%9E%97%E5%8C%B9%E5%85%8B%E5%85%AC%E5%9B%AD.png', '奥林匹克公园作为2008年北京奥运会的主要场馆聚集地，至今仍保留着浓厚的体育氛围。

傍晚沿着蜿蜒的龙形水系散步，水面将鸟巢交错的钢构与水立方晶莹的蓝色膜结构倒映得格外清晰，当灯光逐一点亮，整个园区便沉浸在水光交织的景致中。

北部的森林公园里，湿地与乔木林营造出宁静的自然空间，时常可见白鹭轻掠水面；南部的现代场馆群则在夜色中展现着建筑与光影的巧妙结合。

登上奥林匹克塔远眺，传统中轴线的布局与奥运场馆的当代设计形成和谐对话。

这里不仅是体育历史的见证者，更融入了市民的日常生活——清晨有跑者穿梭于场馆间，午后可见家庭在草坪休闲，夜晚则聚集着捕捉灯光秀的摄影爱好者。

保留至今的奥运火炬广场与冠军墙，默默诉说着当年的拼搏与荣光。

地铁可直达园区，建议安排三小时左右游览，夏季傍晚尤为舒适，既能避开暑热又能欣赏璀璨夜景。

记得穿一双合脚的鞋子，带上相机记录这段建筑与自然共舞的独特体验。

', '北京市朝阳区北辰路奥林匹克公园', 40.0, 116.39, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (64, '北京欢乐谷', '北京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8C%97%E4%BA%AC%E6%AC%A2%E4%B9%90%E8%B0%B7.png', '北京欢乐谷位于东四环边，占地56万平方米，是一座将游乐体验与文化主题深度结合的城市乐园。

园区分为亚特兰蒂斯、爱琴港等七个主题区域，既有极速飞车过山车带来的三秒加速至135公里的强烈推背感，也有奥德赛之旅从26米高处俯冲时激起漫天水花、在夏日阳光下折射出彩虹的生动场景。

除了四十余项游乐设施，每天还有二十多场文化演艺活动——奇幻海洋馆的穹顶之下，鳐鱼悠然游过仿古地中海风格的礁石群；入夜后中央广场上演的欢乐魔方灯光秀，通过两千组智能灯具与音乐喷泉的精准配合，营造出沉浸式的视听体验。

针对家庭游客设计的蚂蚁王国区域设有旋转木马和4D影院，不同年龄层的游客都能在此找到乐趣。

园方在排队区设置了互动游戏装置，智慧导览系统实时更新项目等候时间。

若想获得更顺畅的游玩体验，建议选择早场时段参观热门项目，傍晚时分可至香格里拉区的观景台，欣赏CBD天际线逐渐点亮的城市夜景。

', '北京市朝阳区东四环小武基北路', 39.867, 116.499, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (65, '北京植物园', '北京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8C%97%E4%BA%AC%E6%A4%8D%E7%89%A9%E5%9B%AD.png', '北京植物园静静依偎在西山脚下，这里既是一处植物研究的专业场所，也是市民亲近自然的理想去处。

园中生长着来自全球各地的一万余种植物，四季流转间呈现出不同的景致：春天桃花、海棠与郁金香次第绽放，夏日荷塘清波荡漾竹林成荫，秋日银杏大道洒满金黄，枫林层叠尽染，即便在寒冬，温室里依然绿意盎然。

沿着蜿蜒小径漫步，你会经过古典园林、药用植物园和水生植物区，每个区域都展示着独特的植物群落。

园内还保留着曹雪芹故居等历史建筑，为这片绿色空间增添了人文底蕴。

精心设置的科普展板和互动体验区，让游客在游览中轻松了解植物知识。

无论是亲子自然教育，还是寻求片刻宁静，这里都能满足不同需求。

建议安排半天时间细细游览，热带植物温室和深秋时节的彩叶林尤其值得驻足观赏。

', '北京市海淀区香山路', 39.991, 116.207, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (66, '北京香山公园', '北京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8C%97%E4%BA%AC%E9%A6%99%E5%B1%B1%E5%85%AC%E5%9B%AD.png', '北京香山公园坐落于西郊，曾是清代皇家园林，如今作为自然风景区深受市民与游客青睐。

园内最引人入胜的景致当属秋季，从十月中旬到十一月中旬，数万株黄栌、枫树与银杏由绿转红、渐次染金，自山麓至峰巅铺展出一幅流动的暖色调长卷。

漫步于蜿蜒石径，脚下落叶轻响，空气中浮动着清冽的草木气息。

登临香炉峰极目远望，漫山秋色与京城天际线交织成独特画面。

园中碧云寺、见心斋等古建筑静立于斑斓林影间，青瓦红墙为秋景平添历史厚度。

若想避开人潮，不妨选择清晨或工作日造访，在静谧中感受北方深秋的醇厚意境。

', '北京市海淀区香山南路40号', 39.994, 116.194, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (67, '上海世纪公园', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%8A%E6%B5%B7%E4%B8%96%E7%BA%AA%E5%85%AC%E5%9B%AD.png', '世纪公园坐落于浦东新区中心地带，是上海市区内规模最大的城市公园之一。

从地铁2号线世纪公园站出站步行片刻，便能踏入这片占地超过140公顷的绿色天地。

园内最引人注目的是开阔的草坪区域，每逢晴朗日子，总能看到市民们悠闲地躺在草坪上晒太阳，孩子们欢快地奔跑嬉戏。

贯穿公园的步道将游人引向镜天湖，这片12公顷的水面是上海中心城区最大的人工湖，湖岸垂柳依依，倒映在清澈的湖水中。

湖心小岛常可见白鹭栖息，它们时而掠过水面，为静谧的湖景增添生机。

园内七大主题园区各具特色，乡土田园区展现江南园林风貌，观景平台种植着成排的银杏和香樟。

春日樱花烂漫，秋日银杏金黄，四季变换的景致吸引着摄影爱好者前来捕捉美好瞬间。

公园设计巧妙融合东西方园林精髓，既有中式亭台水榭的雅致，也不乏欧式对称花坛的规整。

站在芳花园旁的景观天桥上，东望可见陆家嘴现代建筑轮廓，西眺则是满园苍翠。

这里不仅是市民日常休闲的重要场所，还定期举办国际音乐烟花节、春季花展等大型活动。

无论是独自散步阅读，还是与家人共度周末时光，世纪公园都能为都市人提供一方惬意的绿色空间。

', '上海市浦东新区锦绣路1001号', 31.221, 121.545, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (68, '上海海洋水族馆', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%8A%E6%B5%B7%E6%B5%B7%E6%B4%8B%E6%B0%B4%E6%97%8F%E9%A6%86.png', '上海海洋水族馆坐落于陆家嘴金融区，是一座以展示全球海洋生物为主题的现代化场馆。

步入亚洲最长的海底观光隧道，弧形亚克力玻璃幕墙将你环绕，超过450种、一万五千余尾海洋生物在身边自在游弋，仿佛置身真实的海底世界。

馆内按地理分区精心设计，从长江流域的中华鲟到亚马逊河的巨骨舌鱼，从澳大利亚的锯鳐到东非湖泊的彩色慈鲷，带你领略五大洲水域的生态多样性。

深海展区采用特殊蓝光照明，模拟800米深处的幽暗环境，发光水母如点点星辰在黑暗中缓缓飘移。

企鹅展区还原了南极岩石海岸的生境，每日定时的喂食时刻让游客能近距离观察企鹅的日常习性。

全馆通过中央循环系统精确控制水体的温度与压力，为来自不同海域的生物提供稳定的生存环境。

', '上海市浦东新区陆家嘴环路1388号', 31.241, 121.504, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (69, '上海自然博物馆', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%8A%E6%B5%B7%E8%87%AA%E7%84%B6%E5%8D%9A%E7%89%A9%E9%A6%86.png', '坐落于静安雕塑公园内的上海自然博物馆，由珀金斯威尔事务所设计成螺旋上升的建筑形态，呼应着生命演化的漫长轨迹。

步入挑高30米的中央大厅，跨越三层楼的马门溪龙骨架巍然矗立，这具来自1.6亿年前的化石静静述说着地球生命的史诗。

馆内十个常设展区以"自然·人·和谐"为主线，从起源之谜到未来之路，完整呈现了自然界137亿年的演化历程。

在生命长河展区，巴掌大小的中华侏罗兽化石作为哺乳动物祖先的代表，见证着恐龙时代幸存者的坚韧；借助AR互动装置，远古生物仿佛在观者眼前重现生机。

极地探索展区将栩栩如生的动物标本与实时科考影像交织，营造出沉浸式的极地环境体验。

馆藏二十九万余件标本中，黄河古象骨架与翼龙化石等珍品尤为珍贵，而专设的儿童探索中心则通过化石挖掘、昆虫观察等互动项目，让年幼访客在游戏中感知自然奥秘。

这座博物馆不仅是认识自然的窗口，更引导人们思考生命与环境的深层联系，使每位参观者都能对蓝色星球的过去与未来产生新的理解。

', '上海市静安区北京西路510号', 31.233, 121.462, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (70, '上海动物园', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%8A%E6%B5%B7%E5%8A%A8%E7%89%A9%E5%9B%AD.png', '上海动物园位于城市西郊，是距离市中心最近的大型野生动物园。

园内规划了生态湿地、灵长类山丘和猛兽山谷等主题展区，生活着大熊猫、金丝猴、扬子鳄等四百多种珍稀动物。

孩子们可以透过玻璃观察孟加拉虎在模拟山林间漫步的身影，也可以在互动区亲手给温顺的羊驼喂食。

动物行为展示区每天有固定时段的讲解，饲养员会分享火烈鸟单腿站立的有趣习性。

游览途中可以搭乘复古小火车穿过波光粼粼的天鹅湖，或是在樱花长廊下小憩。

园中四季景致各异，春日垂丝海棠点缀着动物馆舍，秋日银杏大道铺满金黄落叶。

建议安排四到六小时游览时间，避开周末下午的客流高峰，带孩子的家庭可以租用卡通造型的亲子推车。

', '上海市长宁区虹桥路2381号', 31.191, 121.369, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (71, '共青国家森林公园', '上海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%85%B1%E9%9D%92%E5%9B%BD%E5%AE%B6%E6%A3%AE%E6%9E%97%E5%85%AC%E5%9B%AD.png', '共青国家森林公园虽地处城市中心，却完整守护着近200公顷的天然林地。

行走园中，高耸的水杉与香樟交织成连绵树冠，将车马人声轻轻阻隔。

散落的湖泊如翡翠般点缀林间，天鹅湖畔蜿蜒着2.5公里木栈道，时见白鹭展翅掠过水面，荡开圈圈涟漪。

这里的空气格外清新，负氧离子浓度常年保持在每立方厘米1500-2000个，比闹市区高出三倍有余。

清晨常有人在松涛区慢练太极，午后的林间空地上聚集着野餐的家庭，孩子们则在专属的自然教育区观察昆虫旅馆的动静。

原始湿地栖息着苍鹭、斑嘴鸭等32种鸟类，观鸟墙上的图鉴帮助访客认识这些常住居民。

四季在此留下鲜明印记：春日的樱花径轻絮飘飞，盛夏的荷花塘碧叶连天，秋色浸染银杏大道一片金黄，冬雪为雪松林披上素白衣裳。

园内定期举办的森林音乐会与自然写生活动，让这片城市绿洲不仅滋养着呼吸，更成为人们感知自然律动的生态课堂。

', '上海市杨浦区嫩江路2000号', 31.317, 121.562, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (72, '广州荔枝湾涌', '广州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%B9%BF%E5%B7%9E%E8%8D%94%E6%9E%9D%E6%B9%BE%E6%B6%8C.png', '沿着荔枝湾涌缓缓行走，青石板路在脚下延伸，两岸是成片的岭南传统建筑，青砖灰瓦的民居静静伫立在水边，趟栊门与满洲窗无声记录着西关往昔的风华。

游船从古老的石拱桥下悠然穿过，水面倒映着繁茂榕树垂下的气根，偶有当地居民坐在河畔茶楼里，一边品茶一边闲谈。

这里保留着完整的水乡格局，水道蜿蜒串联起骑楼街市与传统作坊，清晨薄雾轻笼河面，为这幅生动的岭南水乡日常添上朦胧而真实的韵味。

', '广州市荔湾区龙津西路', 23.127, 113.235, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (73, '广州塔二沙岛公园', '广州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%B9%BF%E5%B7%9E%E5%A1%94%E4%BA%8C%E6%B2%99%E5%B2%9B%E5%85%AC%E5%9B%AD.png', '广州塔二沙岛公园坐落在珠江主航道的天然岛屿上，整座岛屿被茂密的绿植覆盖，形成城市中难得的生态空间。

沿着环岛步道行走，一侧是缓缓流淌的江水，另一侧可见星海音乐厅与广东美术馆的建筑轮廓隐现于树影之间。

清晨常有跑者穿梭在榕树成荫的小径上，傍晚时分草坪上不时传来练习乐器的悠扬旋律。

站在临江的观景平台，对岸的广州塔在开阔的水面上显得格外清晰，现代都市景观与自然环境的交融让这里成为市民日常休闲和艺术活动的理想场所。

', '广州市越秀区二沙岛烟雨路', 23.12, 113.313, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (74, '黄埔古港文化村', '广州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E9%BB%84%E5%9F%94%E5%8F%A4%E6%B8%AF%E6%96%87%E5%8C%96%E6%9D%91.png', '黄埔古港文化村静静依偎在珠江入海口，这里曾是海上丝绸之路往来商船停泊的重要港口。

漫步在青石板铺就的巷道中，两侧连绵的岭南传统建筑以青砖灰瓦勾勒出温润轮廓，木雕花窗在光影间流转着时光痕迹。

清代商行的断壁残垣与古码头石阶相偎而立，百年榕树荫蔽着的老茶铺飘来淡淡茶香，墙面斑驳处还残留着昔日商号的刻痕。

港口那座历经风霜的灯塔依然守望着江面，仿佛还能看见当年满载香料瓷器的商船在此停靠卸货。

当地居民至今仍在用蚝壳砌筑的院落里熬制鲜美的艇仔粥，飞针走线延续着广绣技艺，让这座活着的博物馆不仅承载着海上贸易的历史记忆，更延续着岭南水乡特有的生活气息。

', '广州市黄埔区黄埔古港路', 23.103, 113.45, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (75, '华南植物园', '广州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8D%8E%E5%8D%97%E6%A4%8D%E7%89%A9%E5%9B%AD.png', '漫步在华南植物园的蜿蜒小径上，阳光透过层层叠叠的树冠洒下斑驳光影，空气中飘散着木兰与姜花的清浅香气。

作为中国规模最大的南亚热带植物园之一，这片占地四千余亩的园区精心规划了三十八个专类园，从挺拔的棕榈林到珍稀的苏铁丛，八千余种植物在此安然生长。

沿着木兰园的石板路前行，能看见不同季节绽放的花木；转入蕨类植物区，巨大的叶片勾勒出湿润荫凉的生态空间。

温室里模拟的热带雨林环境雾气氤氲，层叠的植被间偶尔传来鸟鸣。

这里不仅是植物保育的重要基地，更通过清晰的导览标识与专业解说，让访客在漫步中了解南亚热带植物的生态特征与保护价值。

', '广州市天河区兴科路723号', 23.183, 113.361, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (76, '东部华侨城', '深圳', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%9C%E9%83%A8%E5%8D%8E%E4%BE%A8%E5%9F%8E.png', '坐落于深圳大鹏湾畔的东部华侨城，是一处融合自然生态与人文体验的综合性旅游区。

三面环山的地形造就了独特的微气候环境，清晨山间常有薄雾缭绕，傍晚时分则能观赏到山海相连的落日景象。

园区占地约九平方公里，分为三大主题区域：大峡谷乐园依山势建造，乘坐缆车缓缓上行时，可将整片海湾风光尽收眼底；茶溪谷保留着原始次生林风貌，沿木栈道行走能听到溪水潺潺与鸟鸣相和；云海谷设有18洞标准高尔夫球场，起伏的果岭与远山轮廓构成层次分明的景观。

八家度假酒店错落分布在山林之间，从以温泉为特色的茵特拉根酒店到充满童趣的瀑布酒店，每间客房都拥有独特的观景视角。

入夜后，在拥有360度旋转观众席的剧院上演的大型舞台剧《天禅》，巧妙融合禅意文化与现代舞台技术。

这里四季各有风情：春天可体验采茶制茶，夏季能在水上项目中消暑，秋季可漫步两公里长的红叶谷，冬季则适合在温泉中欣赏山景。

园区内设有便捷的接驳系统，建议安排两天时间充分感受这个山海相映的度假胜地。

', '深圳市盐田区大梅沙东部华侨城', 22.622, 114.278, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (77, '深圳湾公园', '深圳', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%B7%B1%E5%9C%B3%E6%B9%BE%E5%85%AC%E5%9B%AD.png', '深圳湾公园沿着海岸线铺展开来，将城市生活与自然景致巧妙相连。

漫步在这条滨海绿道上，左侧是开阔的碧蓝海湾，水面在阳光下泛着细碎波光，右侧则是绵延的绿化带，草木葱茏间不时传来鸟鸣。

海风轻柔地拂过面颊，偶尔可见白鹭从水面掠过，为这片宁静增添几分灵动。

天气晴好时，对岸香港元朗和流浮山一带的建筑群清晰可见，现代楼宇在天际线上勾勒出高低错落的轮廓。

傍晚时分尤为动人，夕阳为对岸楼群镀上温暖的金色，与逐渐亮起的深圳湾大桥灯光相互映衬。

这里不仅是市民晨跑骑行的理想场所，也成为游客欣赏深港双城风光的独特观景平台。

', '深圳市南山区深圳湾大道', 22.52, 113.947, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (78, '红树林自然保护区', '深圳', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E7%BA%A2%E6%A0%91%E6%9E%97%E8%87%AA%E7%84%B6%E4%BF%9D%E6%8A%A4%E5%8C%BA.png', '在这座现代都市的中心，一片红树林湿地安然舒展着独特的生态画卷。

沿着蜿蜒的木栈道缓步前行，盘根错节的根系在潮水中舒展，白鹭时而掠过林梢，在蓝天划出优雅弧线。

涨潮时分，整片树林宛如浮岛般轻盈荡漾；退潮后，滩涂显露细腻的纹理，招潮蟹在泥滩间忙碌穿梭。

这里保存着完整的潮间带生态系统，茂密的红树林通过发达的根系净化水质，为众多鸟类和水生生物提供栖息家园。

每日的潮汐更替造就了淹水与露滩交替的独特景致，让访客在都市喧嚣中感受自然韵律的生动演绎。

', '深圳市福田区红树林路', 22.53, 114.016, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (79, '深圳欢乐谷', '深圳', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%B7%B1%E5%9C%B3%E6%AC%A2%E4%B9%90%E8%B0%B7.png', '深圳欢乐谷位于南山区华侨城，占地三十五万平方米，是华南地区颇具规模的主题乐园。

整个园区划分为九个主题区域，从入口处的西班牙广场开始，红砖拱廊与铁艺灯饰勾勒出地中海街景；冒险山绿树成荫，木质过山车在林间蜿蜒穿行；金矿镇重现十九世纪美国西部风貌，淘金器械与木结构建筑营造出粗犷的淘金时代氛围；香格里拉区域融合雪域元素，悬挂式过山车在仿造山岩与经幡间急速回转；飓风湾模拟加勒比海港场景，激流勇进项目从十米高度俯冲而下，激起大片水花；阳光海岸通过细沙铺地和椰树种植，在都市中营造出海滨休闲感；夏季开放的玛雅水公园设有造浪池和彩色滑道；欢乐时光区域汇集旋转木马、碰碰车等经典游乐项目；魔幻城堡则为幼龄游客准备了适合的温和设施。

各区域通过特色建筑、景观植被与背景音效共同构建沉浸环境，日间巡游与夜间灯光秀呈现昼夜不同的游览体验。

乐园还会根据季节推出特色活动，如万圣节主题夜场、圣诞季庆典等，让游客每次到访都能感受到新意。

', '深圳市南山区侨城西街18号', 22.54, 113.985, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (80, '厦门园博苑', '厦门', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8E%A6%E9%97%A8%E5%9B%AD%E5%8D%9A%E8%8B%91.png', '厦门园博苑坐落于杏林湾畔，是一座以海湾为基底的园林博览公园。

园区巧妙利用天然海湾地形，将广阔水域与精致园林融为一体，九座主题岛屿由蜿蜒的桥梁相连，形成园在水中、水在园中的独特景致。

漫步其间，既能欣赏江南园林的亭台水榭，也能领略异国花园的别样风情。

潮水轻拍岸线，白鹭时而掠过水面，远处五缘大桥的轮廓与夕阳相映成趣。

这里不仅汇集了国内外园林艺术的精华，更通过保留原生红树林、完善步道与观景设施，为游客提供舒适的自然体验。

无论是乘电瓶车环游，还是沿滨水步道慢行，都能感受到海湾与园林相得益彰的宁静之美。

', '厦门市集美区环杏前路', 24.614, 118.036, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (81, '集美学村', '厦门', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E9%9B%86%E7%BE%8E%E5%AD%A6%E6%9D%91.png', '漫步在集美学村的石板路上，两侧绿树掩映着红砖建筑，耳边传来课堂的读书声与悠远的钟鸣。

这座由爱国华侨陈嘉庚先生于1913年创建的学村，是中国近代教育史上首个完整规划的人文社区，从幼儿园到大学的校舍错落分布在龙舟池畔。

闽南特色的红瓦翘脊与西洋拱廊在这里和谐共存，形成了独特的嘉庚建筑风格。

清晨可见学生抱着书本穿过林荫校道，傍晚时分居民与游客沿着池边散步闲谈。

陈嘉庚故居、归来堂等历史建筑静静矗立在学村各处，斑驳砖墙记录着创办人倾资办学的往事。

这里不仅是持续运转的教育场所，更是一座承载着中西文化交融印记的活态文化空间，百年来始终保持着书香与生活气息交织的社区氛围。

', '厦门市集美区集美学村', 24.575, 118.104, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (82, '胡里山炮台', '厦门', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%83%A1%E9%87%8C%E5%B1%B1%E7%82%AE%E5%8F%B0.png', '胡里山炮台坐落于厦门岛南端岬角，这座始建于清光绪十七年的海防要塞至今保留着原始格局。

沿着花岗岩城墙缓步而行，斑驳的炮位与蜿蜒的交通壕勾勒出昔日的防御体系，其中那门克虏伯大炮曾是东亚海岸线重要的战略装备。

站在制高点眺望，咸润海风裹挟着涛声拂面而来，180度海景尽收眼底。

晴好时日可见白鹭掠过礁石，对岸金门岛的轮廓浮现在海平线上。

黄昏时分尤为动人，落日熔金洒满海面，归航渔船在粼粼波光中穿行，与百年前守军守望的竟是同一片海域。

作为现存最完整的洋务运动军事遗址，这里通过语音导览系统还原了清代海防部署与厦门海域战役始末。

建议游客先参观史料陈列馆了解背景，再沿滨海步道行至东端观测所，那里保存的方位刻度盘至今仍可清晰辨识海上航迹。

', '厦门市思明区胡里山路2号', 24.427, 118.167, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (83, '泉州台商投资区滨海公园', '泉州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%B3%89%E5%B7%9E%E5%8F%B0%E5%95%86%E6%8A%95%E8%B5%84%E5%8C%BA%E6%BB%A8%E6%B5%B7%E5%85%AC%E5%9B%AD.png', '面向台湾海峡的泉州台商投资区滨海公园沿着海岸线铺展开来，清晨的朝阳将海面染成细碎的金色，傍晚时分则能看到霞光浸染天际的温暖景象。

三点二公里长的步道紧贴海岸蜿蜒延伸，平整的路面适合慢跑、散步或骑行，沿途设置的观景平台和休息长椅让游人能随时驻足，感受海浪轻拍礁石的节奏，聆听海风拂过耳畔的声音。

园内成排的木麻黄和棕榈树既为行人提供荫蔽，也起到了固沙护岸的作用。

这里保持着自然的海岸风貌，退潮时礁石区露出小片滩涂，偶尔可见当地居民赶海的身影。

站在观景台向西眺望，泉州湾跨海大桥的优美弧线横跨在碧波之上。

无论是独自放松还是与亲友共度闲暇，这里都是感受海风、观赏海景的理想场所，建议在日落前两小时到访，既能避开正午的烈日，又能欣赏到海上晚霞的最佳景致。

', '泉州市台商投资区滨海大道', 24.878, 118.706, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (84, '杭州宝石山', '杭州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%9D%AD%E5%B7%9E%E5%AE%9D%E7%9F%B3%E5%B1%B1.png', '登上宝石山的最佳时刻是日落前半小时，此时天光渐暗，整座城市开始悄然变换。

站在蛤蟆峰顶，脚下西湖的千年碧波泛着细碎银光，湖心三岛在暮色中化作深浅不一的剪影。

随着夕阳余晖褪去，城区的灯火从湖滨银泰的现代建筑群向老城区蔓延，街道逐渐连成流动的光带。

晚风掠过保俶塔的飞檐，带来松林与泥土的清新气息，雷峰塔在远处通体明亮，与保俶塔形成跨越时空的对话。

湖面游船划出转瞬即逝的光痕，苏堤六桥的灯串如同散落的珍珠项链。

这个180度观景平台的神奇之处在于，它将自然山水与都市繁华同时纳入视野——西湖的静谧与城市的脉动在此和谐共存。

建议随身携带薄外套抵御山风，但眼前这片融合了千年历史与现代生机的夜景，足以让人沉浸其中忘却微寒。

', '杭州市西湖区曙光路宝石山路口', 30.264, 120.133, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (85, '中国茶叶博物馆', '杭州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%AD%E5%9B%BD%E8%8C%B6%E5%8F%B6%E5%8D%9A%E7%89%A9%E9%A6%86.png', '中国茶叶博物馆静静伫立在杭州西湖边的龙井茶园中，以茶为主题完整呈现了中华五千年的饮茶历史。

走进展厅，汉代墓葬出土的原始茶具首先吸引视线，那些粗糙陶罐表面还隐约可见古人煮茶时留下的烟火印记。

顺着时间轴线漫步，唐代鎏金银茶碾、宋代建窑黑釉兔毫盏、明代紫砂壶等两千余件藏品依次呈现，每件器物都承载着特定时代的品茶习俗。

在茶史厅转角处，复原的宋代茶肆场景格外生动，仿真人像正在演示七汤点茶技法，茶筅搅动时泛起的洁白沫饽，令人联想到古籍记载的茶香韵味。

二楼互动区设有现代茶艺体验空间，参观者可以亲手称量复刻的清代普洱茶饼，通过电子显微镜观察茶叶的微观结构，或在专业茶师指导下学习潮汕工夫茶的二十一式冲泡流程。

博物馆建筑本身也蕴含着茶文化意境，斜坡屋顶收集的雨水经由竹制水槽汇入中庭水池，模拟出茶叶生长所需的湿润环境。

窗外连绵的茶山与室内展陈相映成趣，每逢春季采茶时节，推窗便能闻到远处茶园飘来的清新茶香。

这种将文物陈列、文化传承与自然景观完美融合的设计，让博物馆既守护着茶文化记忆，又成为可触摸的活态茶史。

', '杭州市西湖区龙井路88号', 30.218, 120.111, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (86, '苏州金鸡湖景区', '苏州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%8B%8F%E5%B7%9E%E9%87%91%E9%B8%A1%E6%B9%96%E6%99%AF%E5%8C%BA.png', '苏州金鸡湖景区坐落于工业园区核心地带，这片7.4平方公里的水域与周边现代建筑群相映成趣，形成了独具特色的城市湖泊景观。

白天可见商务楼群的轮廓倒映在如镜的湖面上，游船驶过划出粼粼波光，环湖分布的音乐喷泉、水上摩天轮和文化艺术中心等设施为游客提供了丰富的休闲选择。

当夜幕降临，湖畔灯光渐次点亮，东方之门与国金中心勾勒出璀璨的城市天际线，月光码头沿岸餐厅透出温馨的光影。

每周五晚的音乐喷泉表演将水幕与灯光巧妙融合，湖心岛屿的灯光秀宛若繁星点缀水面。

从李公堤到湖滨大道，不同观景点都能捕捉到这座现代湖泊在昼夜交替中呈现的多样风情。

', '苏州市工业园区金鸡湖畔', 31.312, 120.724, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (87, '同里古镇', '苏州', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%90%8C%E9%87%8C%E5%8F%A4%E9%95%87.png', '同里古镇的脉络由十五道清流编织而成，这些河道将古镇自然分割为七座小岛，四十九座古桥又轻巧地将它们重新缝合。

晨雾初起时，船娘的木橹划开平静的水面，欸乃声惊醒了栖息在三元桥墩的白鹭。

退思园是古镇的精神所在，园主任兰生当年辞官归乡，取《左传》中"退思补过"之意营造此园。

园林打破传统格局，以横向延展的布局徐徐铺陈，亭台楼阁仿佛从水面上生长出来，每到春日紫藤垂花时节，整座园子便笼罩在朦胧的紫晕之中。

穿心弄的青石板路仅容一人通过，两侧斑驳的粉墙夹出一道幽深巷道，脚步声在石板上激起清越的回响。

明清街的老茶馆里飘着碧螺春的清香，临窗可见摇橹船划过水面留下的绵长波纹。

当夕阳为太平桥、吉利桥、长庆桥镀上金辉，当地老人会坐在桥栏上用吴语闲聊，延续着"走三桥"的传统——走过这三座桥，就意味着把平安、吉祥与幸福都收进了心怀。

入夜后，红灯笼在风中轻摇，倒影在水面碎成流动的光点。

每周三晚，退思园的水上戏台都会响起昆曲《牡丹亭》的婉转唱腔，让人恍然领悟陈从周教授所言"贴水园的极致"的含义。

这里的每块青石板都镌刻着时光，廊下的画眉在笼中轻啼，裱画店里的老师傅依然用传统技艺装裱着水墨江南的韵味。

', '苏州市吴江区同里镇', 31.155, 120.723, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (88, '南京牛首山文化旅游区', '南京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8D%97%E4%BA%AC%E7%89%9B%E9%A6%96%E5%B1%B1%E6%96%87%E5%8C%96%E6%97%85%E6%B8%B8%E5%8C%BA.png', '南京牛首山文化旅游区位于金陵城南，将悠久的佛教传承与秀美的山林景致自然融合。

沿着青石阶漫步而上，两旁古树苍翠，时而能听见远处寺院的钟声在山谷中悠悠传响。

山顶的佛顶宫建筑群以唐代风格为基调，结合现代设计手法，金色穹顶在日光下流转着温润光泽。

西侧的郑和墓遗址静立于松柏之间，默默见证着古代海上丝绸之路的辉煌。

每逢春日，山间遍野的桃花与杜鹃与青峰相映，站在观景台可望见秦淮河如一条素练蜿蜒远去。

清晨的禅意步道尤为宜人，薄雾轻笼着寺庙的飞檐，空气中隐约浮动着檀香的气息。

明代抗金故垒保存完好，斑驳的石墙记录着历史的沧桑。

无论探寻佛教文化，还是沉浸自然山水，这里都能让人在宁静中获得慰藉。

建议安排半天行程，先参观文化遗迹，再循山脊步道慢行，最后在禅茶院小憩，品一盏清雅的雨花茶，细细体会这座千年名山的深厚底蕴。

', '南京市江宁区宁丹大道18号', 31.901, 118.806, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (89, '南京老门东历史文化街区', '南京', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8D%97%E4%BA%AC%E8%80%81%E9%97%A8%E4%B8%9C%E5%8E%86%E5%8F%B2%E6%96%87%E5%8C%96%E8%A1%97%E5%8C%BA.png', '漫步在南京老门东历史文化街区，脚下的青石板路蜿蜒向前，两侧错落有致的建筑群以各自的方式记录着时光的痕迹。

这里完整保留了明清时期的马头墙与民国时期的西式窗棂，灰瓦白墙的间隙中偶尔探出几株梧桐，为这片历史街区增添了自然的生机。

沿着箍桶巷往南走，建筑细节的微妙变化逐渐显现——从明代民居的木质雕花到民国洋楼的彩色玻璃，每个转角都呈现出不同时代的建筑语言。

这些建筑大多仍维持着原有的使用功能，沿街商铺里飘出桂花鸭的香气，老字号茶社的铜壶冒着热气，让历史不再是静止的展品，而是融入日常的生活场景。

街区内保存完好的三进式宅院尤其值得留意，天井里的水缸倒映着飞檐翘角，完整展现了江南民居的居住智慧。

转角处一栋民国银行旧址的花岗岩立面与铸铁栏杆，则默默见证着上个世纪东西方文化的交融。

夜幕降临时，暖黄色的灯光轻柔勾勒出建筑轮廓，游人可以在百年梧桐下的长椅上小坐，感受这座六朝古都特有的时空交错。

老门东不只是一个观光景点，更是一本立体的建筑史书，让每位到访者在漫步中读懂南京的城市记忆。

', '南京市秦淮区剪子巷老门东', 32.014, 118.8, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (90, '成都人民公园', '成都', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%88%90%E9%83%BD%E4%BA%BA%E6%B0%91%E5%85%AC%E5%9B%AD.png', '成都人民公园位于城市中心，是体验当地慢节奏生活的理想去处。

清晨的阳光透过茂密的香樟树，在石板路上投下斑驳光影，晨练的人们或散步或打太极，鸟鸣声与舒缓的音乐交织在一起。

鹤鸣茶社临湖而设，竹椅木桌错落有致，盖碗茶飘着茉莉清香，当地人常在此品茶闲聊、打牌消遣。

只需二十元就能点杯茶，观看老师傅提着长嘴铜壶娴熟地穿梭添水。

湖面上彩色游船轻轻漂荡，孩童的欢笑声随波扩散。

每逢周末，相亲角便聚满为子女婚事奔走的父母，树枝上悬挂的征婚资料随风轻扬。

掏耳朵的老手艺人仍在梧桐树下服务，坝坝舞的旋律回荡在园中，处处流露着成都人从容的生活态度。

', '成都市青羊区少城路12号', 30.667, 104.063, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (91, '锦里古街', '成都', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E9%94%A6%E9%87%8C%E5%8F%A4%E8%A1%97.png', '沿着青石板路漫步锦里古街，两侧灰瓦木楼的川西民居错落有致，檐角垂挂的红灯笼随风轻摇。

这条街以三国历史为脉络，将诸葛亮屯兵、刘备结义等典故融入砖瓦之间，与相邻的武侯祠形成时空呼应。

午后阳光斜照时，糖画艺人手腕轻转便勾勒出晶莹的飞凤游龙，老茶馆飘散的茉莉茶香与戏台铿锵的川剧锣鼓交织相融。

银器铺里叮当的敲打声应和着蜀绣工坊内穿梭的彩线，采耳师傅手持云刀在竹椅间巡回服务。

当暮色渐染天际，千百盏灯笼次第点亮，青石板倒映着温润的橘光，三国风云与市井烟火在此刻悄然相会，还原出老成都巷陌间鲜活的生活长卷。

', '成都市武侯区武侯祠大街231号', 30.643, 104.049, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (92, '重庆洪崖洞民俗风貌区', '重庆', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E9%87%8D%E5%BA%86%E6%B4%AA%E5%B4%96%E6%B4%9E%E6%B0%91%E4%BF%97%E9%A3%8E%E8%B2%8C%E5%8C%BA.png', '重庆洪崖洞民俗风貌区依山傍水而建，十一层高的吊脚楼群沿着嘉陵江岸的陡峭崖壁层层攀升，错落有致的建筑轮廓与自然山势完美融合。

这些延续巴渝传统风格的建筑通过蜿蜒的石阶和小巷相连，白天可见青瓦木墙的细腻构造，入夜后万千灯火渐次点亮，整片建筑宛如悬于崖壁的玲珑宫阙，与江面倒影交相辉映。

站在千厮门大桥远望，流动的灯光与江中游船构成一幅动态画卷。

从底层飘着椒麻香气的美食街巷，到顶层可俯瞰两江交汇的观景平台，每层都藏着独特体验——既能品尝正宗的重庆小面，也能在传统工艺品店铺挑选伴手礼，或是在露台咖啡馆静观江流。

建议黄昏时分到访，此时既能捕捉建筑在白日里的质朴细节，又能目睹灯火逐层亮起的梦幻转变。

沿着山城步道缓步而行，每个转角都会呈现不同的景致组合，江水、灯光与建筑轮廓在山势起伏间构成无限变化的空间韵律。

', '重庆市渝中区嘉陵江滨江路88号', 29.5636, 106.576, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (93, '解放碑步行街', '重庆', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%A7%A3%E6%94%BE%E7%A2%91%E6%AD%A5%E8%A1%8C%E8%A1%97.png', '矗立于渝中半岛的解放碑步行街始终是重庆流动的脉搏，这座历经抗战岁月的纪念碑如今静静伫立在环形商业广场中央，Gucci的橱窗与星巴克的招牌掩映在苍劲的黄葛树下，碑顶传来的钟声和轻轨穿梭楼宇的震动在空气中轻轻共振。

游客们常伸手触碰碑身镌刻的“抗战胜利纪功碑”字迹，一转身便看见肩扛扁担的棒棒军与举着自拍杆的年轻人擦肩而行。

当夜色浸染山城，对岸洪崖洞的灯火如叠叠宫灯悬于江畔，而步行街穹顶的霓虹光束则勾勒出未来感的轮廓。

本地人习惯把这里当作相约的老据点，初访者则通过它领会重庆独特的地形密码——从碑座往任意方向走上几百米，或许要爬一段陡坡，穿过某栋居民楼的廊道，甚至踏出二十二层电梯才发现身在平街，这种空间错落感正是山城最生动的注脚。

', '重庆市渝中区解放碑', 29.5573, 106.5762, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (94, '南山一棵树观景台', '重庆', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8D%97%E5%B1%B1%E4%B8%80%E6%A3%B5%E6%A0%91%E8%A7%82%E6%99%AF%E5%8F%B0.png', '南山一棵树观景台坐落在南岸区半山腰，恰好与渝中半岛隔江相望。

每当夜幕降临，整座山城仿佛被点亮——长江与嘉陵江在脚下蜿蜒交汇，江面倒映着两岸建筑的轮廓光带，渝中半岛如同浮在两江之间的灯火岛屿。

洪崖洞层层叠叠的金色檐角与对岸来福士晶莹的空中连廊遥相呼应，勾勒出传统与现代交融的天际线。

这里的高度经过巧妙选择，既不会因过于遥远而削弱城市的烟火气，又能完整捕捉两江环抱半岛的地理奇观。

许多摄影爱好者偏爱在黄昏时分前来，看天色由蓝转黛，城市灯光如星子般渐次苏醒。

他们用镜头记录的不仅是山城独特的立体景观，更是江水千年流淌中见证的人间故事。

', '重庆市南岸区南山植物园附近', 29.533, 106.61, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (95, '西安城墙永宁门', '西安', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E8%A5%BF%E5%AE%89%E5%9F%8E%E5%A2%99%E6%B0%B8%E5%AE%81%E9%97%A8.png', '站在永宁门下，青灰色城砖在日光中泛出柔和光泽，这座始建于明洪武年间的城门完整保留着瓮城结构与双重门楼格局。

缓步登上马道，脚下坡道历经六百年风雨洗礼，砖缝间青苔悄然记录着时光流转。

登上十二米高的城墙顶端，视野顿时开阔，南望可见现代都市的天际线，北眺则能捕捉钟鼓楼沉稳的轮廓。

十四米宽的墙顶足以容纳四驾马车并行，如今已成为环城绿道，租一辆单人自行车在总长十三点七四公里的城墙上徐徐前行，车轮碾过凹凸墙砖发出特有的声响。

沿途九十八座敌楼依然保持着明代军事防御体系的原始布局，箭窗木雕虽已斑驳，仍可窥见当年精工细作。

傍晚时分登城最为相宜，落日为垛口勾勒金边，城墙内侧老槐树随风轻摇，护城河面倒映着渐次亮起的宫灯。

每周四晚八点举行的仿古开城仪式上，身着明光铠的武士手持长戟列队而行，令人恍若回到百千家似围棋局的长安盛景。

建议从南门广场进入，租车点设在城门内东西两侧，春秋清晨游人稀少可独享宁静，夏季夜骑则能感受晚风轻拂与璀璨灯光，全程骑行约两小时，各城门均可就近还车。

', '西安市碑林区南门外环城南路', 34.255, 108.946, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (96, '大唐芙蓉园', '西安', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%A4%A7%E5%94%90%E8%8A%99%E8%93%89%E5%9B%AD.png', '漫步在大唐芙蓉园中，仿佛一步踏入千年前的长安盛景。

这座占地千亩的唐代文化主题园林以曲江池遗址为基底，完整再现了盛唐时期的皇家园林格局。

紫云楼巍然屹立，俯瞰着波光荡漾的湖面；杏园内花木扶疏，重现了唐代科举放榜时的热闹场面；每晚在华清池畔上演的《梦回大唐》水幕表演，运用现代光影技术还原唐代宫廷乐舞的华美景象。


园中每条回廊的彩绘均参照敦煌壁画精心复原，每座亭台的斗拱结构严格遵循唐代建筑规制。

春日可体验曲江流饮的雅致，夏日能欣赏荷塘月色的清幽，秋日可见银杏叶铺就的金色小径，冬日则能感受雪花轻覆歇山顶的宁静。

这里不仅是了解唐代建筑、服饰、礼仪的活态博物馆，更通过定时巡游的唐代仪仗队、非遗技艺展示和节气民俗活动，让沉睡的历史变得生动可感。


建议在黄昏时分登上望春阁，看夕阳为唐代风格建筑群勾勒出金色轮廓，或选个雨天漫步九曲回廊，聆听雨打芭蕉的韵律，体味唐人笔下的诗意情境。

园区每周举办的唐代茶道、香道体验课程，让人在袅袅茶香中领略大唐的生活美学。

', '西安市雁塔区曲江芙蓉西路99号', 34.21, 108.98, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (97, '曲江池遗址公园', '西安', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%9B%B2%E6%B1%9F%E6%B1%A0%E9%81%97%E5%9D%80%E5%85%AC%E5%9B%AD.png', '曲江池遗址公园位于西安城南，这里曾是汉唐皇家园林的精华所在，如今以现代城市公园的形态重现了千年前的园林格局。

步入园区，复原的曲江水面首先展现在眼前，湖岸线蜿蜒舒展，垂柳与仿唐亭台错落有致，黑鹅游过芦苇丛时，水面泛起的涟漪仿佛还留存着盛唐的气息。

黄昏时分，夕阳为九曲桥描上金边，与远方大雁塔的剪影共同构成一幅宁静的画面。

园内保留着多处考古现场，玻璃罩下的唐代池底遗址旁设有解说牌，清晰呈现不同朝代的地层堆积；特别值得一提的是依据古籍复原的“曲江流饮”景观，通过精密水循环系统再现了唐代文人曲水流觞的雅趣。

这里既是市民晨练散步的日常场所，也是汉服爱好者钟爱的取景地，春日樱花、夏季荷花、秋日银杏与冬季雪亭，四季景致与唐风建筑相互映衬。

建议从南门进入，沿湖岸步道顺时针游览，全程约两小时，傍晚时分的光影尤为动人。

', '西安市雁塔区曲江新区芙蓉东路', 34.219, 108.99, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (98, '昆明滇池海埂大坝', '昆明', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%98%86%E6%98%8E%E6%BB%87%E6%B1%A0%E6%B5%B7%E5%9F%82%E5%A4%A7%E5%9D%9D.png', '每年冬季，随着西伯利亚寒流如期而至，成千上万的红嘴鸥便会飞临滇池这片水域。

它们成群盘旋于湖面之上，时而轻盈俯冲觅食，时而停驻在游人伸出的掌心与肩头。

立于海埂大坝远眺，滇池波光潋滟如碎银铺展，远处西山的“睡美人”轮廓在薄雾中若即若离。

晨光初露时，湖面泛起淡金光泽，红嘴鸥清亮的鸣叫与轻柔的浪声彼此呼应；日暮时分，斜阳将整片水域晕染成温暖的金红色，成群归巢的鸥鸟在霞光中划出流动的剪影。

这里不仅是观赏红嘴鸥迁徙盛况的理想之地，更凝聚着昆明四季温润的气候特质、高原湖泊的开阔气象，以及人与候鸟之间那份从容共处的自然韵律。

', '昆明市西山区海埂大坝', 24.954, 102.66, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (99, '石林风景区', '昆明', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E7%9F%B3%E6%9E%97%E9%A3%8E%E6%99%AF%E5%8C%BA.png', '石林风景区完整展现了喀斯特地貌的漫长演化历程，这片由石灰岩构成的奇观历经亿万年雨水侵蚀与地质变迁，形成了密集分布的灰色石峰群。

穿行其间，高耸的石柱如森林般错落矗立，部分岩峰可达四十米高度，表面布满水流长期冲刷形成的纵向沟痕。

阳光透过石隙洒落斑驳光影，狭窄的通道仅容单人通行，营造出穿行天然迷宫的独特体验。

这里既是解读地质演变的生动范本，也是能直观感受自然雕琢力量的户外课堂，游客可通过铺设完善的步道系统安全探索这片地质奇观。

', '昆明市石林彝族自治县石林镇', 24.821, 103.332, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (100, '南宁青秀山风景区', '南宁', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8D%97%E5%AE%81%E9%9D%92%E7%A7%80%E5%B1%B1%E9%A3%8E%E6%99%AF%E5%8C%BA.png', '青秀山作为南宁的城市绿肺，常年吸引着市民前来放松身心。

沿着平缓的山路漫步，观音禅寺的轮廓渐渐清晰，空气中偶尔飘来香火的味道，为山林增添了几分宁静。

登上龙象塔远眺，蜿蜒的邕江仿佛一条碧色丝带环绕城区，城市天际线在茂密的亚热带植物间时隐时现。

园中保留着明代造园手法，三角梅四季不败，老榕垂下的气根宛如轻摇的帘幕。

这里既有古老的佛教遗迹沉淀着时光，也设置了观景台让视野更加开阔——清晨常见老人在棕榈树下打着太极，年轻人喜欢在天池畔的草坪上小聚，孩子们则在十二生肖石刻间寻找熟悉的动物形象。

当落日余晖为望江亭的琉璃瓦镀上暖光，便能体会到南宁人为何将这片山水视为生活中不可或缺的栖息地。

', '南宁市青秀区凤岭南路6-6号', 22.786, 108.374, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (101, '北海银滩', '北海', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E5%8C%97%E6%B5%B7%E9%93%B6%E6%BB%A9.png', '北海银滩的沙滩由亿万年前沉积的石英砂构成，沙粒细腻均匀，轻轻一捧便会如流水般从指缝间滑落。

退潮后，数公里长的沙滩在日照下泛出柔和的银白色光泽，这正是它得名的缘由。

这里的潮汐平缓，海底坡度极为和缓，即使向海中行走百米，水深也仅及腰部。

清晨退潮时分，沙滩上会留下嵌着贝壳的波纹痕迹，当地渔民常带着传统工具在浅滩赶海。

午后阳光为整片海滩镀上暖金色，赤脚漫步时能感受到细沙包裹脚踝的温热触感。

日落前后，湿润的滩涂倒映出天空变幻的色彩，形成如镜面般的景致。

作为天然海滨浴场，这里全年平均水温保持在23摄氏度左右，夏季海风带着微咸的清凉拂面而来。

沿岸生长的木麻黄防风林错落有致，树荫下常有家庭铺开野餐垫享受海风。

向西步行约八百米，可见仍在使用的传统渔船码头，清晨时常能遇见刚靠岸的渔船，直接购买到新鲜捕捞的海产。

', '北海市银海区银滩镇', 21.445, 109.13, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (102, '三亚大东海景区', '三亚', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E4%B8%89%E4%BA%9A%E5%A4%A7%E4%B8%9C%E6%B5%B7%E6%99%AF%E5%8C%BA.png', '三亚大东海是这座城市最早对外开放的海滨浴场之一，它静静见证了几十年来热带滨海旅游的变迁。

月牙状的海湾绵延近两公里，沙滩平缓地向海中延伸，细软的白沙在日照下泛出柔和光泽，特别适合不谙水性的游人安心亲近海浪。

清晨常见当地人在微凉的海水中晨泳锻炼，午后斜阳穿过岸边的椰林，在沙滩上投下流动的光斑。

虽紧邻市区，海水却保持着难得的清澈，站在及膝深的浅滩就能望见成群的小鱼在珊瑚礁间游弋。

完善的更衣冲洗设施与常年值守的救生员，让这片老牌海滨始终保持着亲切可靠的氛围。

退潮时分，礁石区渐渐露出水面，寄居蟹和海星在岩缝间时隐时现，成为赶海人最期待的探索时刻。

', '三亚市吉阳区大东海旅游区', 18.234, 109.513, '');
INSERT INTO "scenic" ("id", "name", "city", "cover_image", "description", "address", "latitude", "longitude", "audio_url") VALUES (103, '海口万绿园', '海口', 'https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/%E6%B5%B7%E5%8F%A3%E4%B8%87%E7%BB%BF%E5%9B%AD.png', '沿着海口湾绵延展开的万绿园，是一座与城市肌理紧密相连的滨海生态空间。

八十三公顷的园区内错落生长着近万株热带植物，高耸的椰林与舒展的榕树交织出深浅不一的绿色层次。

清晨的滨海步道上常见晨跑者穿梭而过，棕榈叶在朝晖中投下细碎光影；黄昏时分，人们三三两两坐在临海草坪上，看夕阳缓缓沉入琼州海峡，将海面晕染成温暖的琥珀色。

园中分布着多个功能区域：热带植物区聚集了海南本土树种，蕨类在荫生区的石径旁蔓生；儿童乐园飘荡着清脆的笑语，健身区则有太极拳爱好者迎着海风舒展身形。

那条贯穿东西的滨海长廊尤为独特，行走其间，左侧是层层叠叠的植物群落，右侧可见鸥鸟掠过泛着细碎银光的海面。

作为海口规模最大的开放式公园，这里既是城市呼吸的绿肺，也是承载市民日常的公共庭院。

无论您想来慢跑观海，或是单纯寻找树荫下的休憩角落，这片滨海绿洲都能提供恰如其分的自在时光。

', '海口市龙华区滨海大道', 20.034, 110.315, '');
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
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('visited', 28);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('trip_plan', 18);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('favorite', 33);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('cart_item', 38);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('order_main', 15);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('order_item', 29);
INSERT INTO "sqlite_sequence" ("name", "seq") VALUES ('user', 34);
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
INSERT INTO "trip_plan" ("id", "user_id", "title", "start_date", "end_date", "source", "content", "create_time") VALUES (9, 20, '北京慢游周末计划', '2025-05-17', '2025-05-18', 'MANUAL', '{"days":[{"date":"2025-05-17","spots":[17,21]},{"date":"2025-05-18","spots":[19,20]}]}', '2025-05-07 08:30:00');
INSERT INTO "trip_plan" ("id", "user_id", "title", "start_date", "end_date", "source", "content", "create_time") VALUES (10, 20, '魔都夜色打卡', '2025-06-01', '2025-06-02', 'AI', '{"routes":[{"day":1,"spots":[22,23,24]},{"day":2,"spots":[25,26]}]}', '2025-05-09 11:15:00');
INSERT INTO "trip_plan" ("id", "user_id", "title", "start_date", "end_date", "source", "content", "create_time") VALUES (11, 21, '粤港澳亲子行', '2025-05-25', '2025-05-28', 'MANUAL', '{"days":[{"date":"2025-05-25","spots":[27,31]},{"date":"2025-05-26","spots":[28]},{"date":"2025-05-27","spots":[31,33]}]}', '2025-05-08 19:40:00');
INSERT INTO "trip_plan" ("id", "user_id", "title", "start_date", "end_date", "source", "content", "create_time") VALUES (12, 21, '江南水乡家庭游', '2025-05-06', '2025-05-07', 'MANUAL', '{"days": [{"date": "2025-05-06", "spots": [92, 80, 37]}, {"date": "2025-05-07", "spots": [26, 88, 44]}]}', '2025-04-22 00:03:30');
INSERT INTO "trip_plan" ("id", "user_id", "title", "start_date", "end_date", "source", "content", "create_time") VALUES (13, 20, '京津人文周末', '2025-05-29', '2025-05-30', 'MANUAL', '{"days": [{"date": "2025-05-29", "spots": [102, 56, 75]}, {"date": "2025-05-30", "spots": [22, 70, 52]}]}', '2025-04-16 22:50:30');
INSERT INTO "trip_plan" ("id", "user_id", "title", "start_date", "end_date", "source", "content", "create_time") VALUES (14, 20, '珠三角骑行 + 美食', '2025-05-19', '2025-05-21', 'AI', '{"days": [{"date": "2025-05-19", "spots": [36, 76, 22]}, {"date": "2025-05-20", "spots": [89, 66, 61]}, {"date": "2025-05-21", "spots": [80, 60, 91]}]}', '2025-05-07 17:23:54');
INSERT INTO "trip_plan" ("id", "user_id", "title", "start_date", "end_date", "source", "content", "create_time") VALUES (15, 21, '厦门禅修休闲计划', '2025-04-20', '2025-04-24', 'MANUAL', '{"days": [{"date": "2025-04-20", "spots": [73, 83, 54]}, {"date": "2025-04-21", "spots": [52, 32, 40]}, {"date": "2025-04-22", "spots": [70, 72, 32]}, {"date": "2025-04-23", "spots": [84, 30, 79]}, {"date": "2025-04-24", "spots": [65, 101, 49]}]}', '2025-04-24 07:00:44');
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
  "avatar_url" TEXT DEFAULT 'http://139.59.227.54:5001/static/avatarImage/default_avatar.jpg',
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
INSERT INTO "user" ("id", "login_type", "username", "password", "phone", "email", "nickname", "avatar_url", "wx_unionid", "wx_openid", "wx_access_token", "wx_refresh_token", "wx_token_expires_at") VALUES (20, 'LOCAL', 'test01', 'scrypt:32768:8:1$fTTGg6t3ktw2p0pu$be314cffa8eed8caa5b1632d31fed6c0bf2c159e89db9246e4b34e661f8e4507762e23f999ede07512150f10bc644171c472b442333c1a47bc7650229e05522e', '13800000000', 'demo@example.com', NULL, 'https://images.pexels.com/photos/185933/pexels-photo-185933.jpeg', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "user" ("id", "login_type", "username", "password", "phone", "email", "nickname", "avatar_url", "wx_unionid", "wx_openid", "wx_access_token", "wx_refresh_token", "wx_token_expires_at") VALUES (21, 'LOCAL', 'test02', 'scrypt:32768:8:1$eEnxFQteKyeooqYv$72df9d99afb19b2c49e2d21f2fd9381d83dd7becb324092ea69c332a5e212ccf558693a49bc4e34ac488cd5a74493d9a836bcc28cfe5aa59dd1c78a221b2734d', '19353540720', '44739528@qq.com', NULL, 'https://images.pexels.com/photos/1051078/pexels-photo-1051078.jpeg', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "user" ("id", "login_type", "username", "password", "phone", "email", "nickname", "avatar_url", "wx_unionid", "wx_openid", "wx_access_token", "wx_refresh_token", "wx_token_expires_at") VALUES (28, 'LOCAL', '焦梓豪', 'scrypt:32768:8:1$dTs2Rqm7vIbThX4c$ea6a282375d2b87b5113362fe1cf62b5cb9476598bb91343738f7bb4015c19fcf94ff76959c832a3b588600688a17495bfde8573341d7d9230b032c3c5811931', '13850056409', '1046220903@qq.com', '焦梓豪', 'https://images.pexels.com/photos/163185/old-retro-antique-vintage-163185.jpeg', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "user" ("id", "login_type", "username", "password", "phone", "email", "nickname", "avatar_url", "wx_unionid", "wx_openid", "wx_access_token", "wx_refresh_token", "wx_token_expires_at") VALUES (33, 'WECHAT', 'Justtyn', NULL, NULL, NULL, 'Justtyn', 'https://thirdwx.qlogo.cn/mmopen/vi_32/WZ2Wvthj3YAMeATSDRgoa9rNhebvzHCUrQDG2G3gtWuPVxPOw4fumDiaUXVeD59szWoOTpXeFmCOnEhhZvujYMAsx6gh54G5OEt7C7ibKZYro/132', 'oYLbY63qSUjuZn1PxUOeQJ3yQXCQ', 'oZufY6yX1Lz1mjuwhd2TsIdxE7X0', '98_EaX20JGh4tVbHvstn9WyT0eOv-mo_pTQO3_XW1ZetHMs8fZZMEWHnbBACW6hZNss85GhsI5Uanp-tvYtK5RZefsQWM90SwfMuiU5RwHS9h4', '98_C43bu4uB7arWu2QvnlPsog2SL2COVB8oegz8WfmzCOtx0tpmtOlJCZqWvnUop4W7XmH70tMjZ6GbRUd1FsPOastL8Wqr3a19JEGwxenChi0', '2025-11-18 07:47:59');
INSERT INTO "user" ("id", "login_type", "username", "password", "phone", "email", "nickname", "avatar_url", "wx_unionid", "wx_openid", "wx_access_token", "wx_refresh_token", "wx_token_expires_at") VALUES (34, 'WECHAT', 'Justyn', NULL, NULL, NULL, 'Justyn', 'https://thirdwx.qlogo.cn/mmopen/vi_32/PiajxSqBRaELh75aGnEvbxpmxF81WGoYADF5lUlg2wtHiaVa0FiaTkawXhOuiar7hhvHU8uhv92oweibX5K2eS4sOI9jpVUygz8lRMdDmkxsMX4yyWrEukicRv4A/132', 'oYLbY6-HB4cqGb06uj-JaDC6medM', 'oZufY68ikjvv9IKA4qZHPo3F8MT4', '98_abCm5yseudJrCT36i3B6DSPVQsbcQVzu4W8U4tcGibz7nR60bBdqcCJMVp6aDBet0OCuS20-F1rjVnSarWB78fra8yFsrxDWfEI45HpEvD4', '98_qGbaKqnN2ns-5pn_MVoG24PBHdUq5C1ZfOgdHadRedUWqVp7aPR85tpgNAvrvImi2O4sEBtqKVl199lrf-6jUJicwqirdIUz5qtYV_7yaSw', '2025-11-18 07:51:15');
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
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (11, 20, 17, '2025-04-28', 5);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (12, 20, 22, '2025-05-02', 4);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (13, 20, 32, '2025-05-05', 5);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (14, 21, 23, '2025-04-20', 4);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (15, 21, 31, '2025-04-30', 5);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (16, 21, 28, '2025-05-03', 3);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (17, 21, 25, '2025-04-26', 5);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (18, 20, 51, '2025-04-23', 5);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (19, 21, 78, '2025-05-19', 4);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (20, 20, 100, '2025-06-12', 3);
INSERT INTO "visited" ("id", "user_id", "scenic_id", "visit_date", "rating") VALUES (21, 20, 82, '2025-04-15', 3);
COMMIT;

-- ----------------------------
-- Auto increment value for cart_item
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 38 WHERE name = 'cart_item';

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
UPDATE "main"."sqlite_sequence" SET seq = 33 WHERE name = 'favorite';

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
UPDATE "main"."sqlite_sequence" SET seq = 29 WHERE name = 'order_item';

-- ----------------------------
-- Auto increment value for order_main
-- ----------------------------
UPDATE "main"."sqlite_sequence" SET seq = 15 WHERE name = 'order_main';

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
UPDATE "main"."sqlite_sequence" SET seq = 18 WHERE name = 'trip_plan';

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
UPDATE "main"."sqlite_sequence" SET seq = 34 WHERE name = 'user';

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
UPDATE "main"."sqlite_sequence" SET seq = 28 WHERE name = 'visited';

-- ----------------------------
-- Indexes structure for table visited
-- ----------------------------
CREATE INDEX "main"."idx_visited_user"
ON "visited" (
  "user_id" ASC
);

PRAGMA foreign_keys = true;
