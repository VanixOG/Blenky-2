/*
 * Copyright 2019 John Grosh (john.a.grosh@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.giveawaybot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import com.jagrosh.giveawaybot.database.Database;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.EnumSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Checker
{
    /**
     * Runs the application as a premium updater
     * @throws Exception 
     */
    public static void main() throws Exception
    {
        LoggerFactory.getLogger("Checker").info("Checker starting.");
        
        Config config = ConfigFactory.load();
        long premiumServerId = config.getLong("checker-server");
        
        // connects to the database
        Database database = new Database(config.getString("database.host"), 
                                       config.getString("database.username"), 
                                       config.getString("database.password"));
        
        // sends starting message
        WebhookClient webhook = new WebhookClientBuilder(config.getString("webhook")).build();
        webhook.send(Constants.TADA + " Starting checker...");
        
        JDA jda = new JDABuilder(config.getString("checker-token"))
                .setStatus(OnlineStatus.IDLE)
                .setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, 
                                                  CacheFlag.EMOTE, CacheFlag.VOICE_STATE))
                .build().awaitReady();
        
        webhook.send(Constants.TADA + " Checker ready! `" + premiumServerId + "`");
        
        // main checker loop
        while(true)
        {
            Thread.sleep(1000 * 60);
            
            // update premium levels
            database.premium.updatePremiumLevels(jda.getGuildById(premiumServerId));
        }
    }
}
