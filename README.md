# FoodEval
**技术栈**：SpringBoot+Mysql+Mybatis-plus+redis+Hutool+Lombok\
**项目描述**：基于Redis + Springboot的点评APP,实现了短信验证码登录、查找店铺、秒杀优惠券、发表点评、关注推送的完整业务流程\
使用Redis解决了在集群模式下的Session共享问题,使用拦截器实现用户的登录校验和权限刷新\
基于Cache Aside模式解决数据库与缓存的一致性问题\
使用Redis对高频访问的信息进行缓存，降低了数据库查询的压力,解决了缓存穿透、雪崩、击穿问题使用Redis + Lua脚本实现对用户秒杀资格的预检，同时用乐观锁解决秒杀产生的超卖问题\
使用Redis分布式锁解决了在集群模式下一人一单的线程安全问题\
基于stream结构作为消息队列,实现异步秒杀下单\
使用Redis的ZSet数据结构实现了点赞排行榜功能,使用Set 集合实现关注、共同关注功能\
