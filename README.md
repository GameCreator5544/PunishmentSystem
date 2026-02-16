[![Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_64h.png)](https://modrinth.com/plugin/punishmentsystem)

# Punishments System
---
An easy to use punishment system.

## Features
- A /punish commands with pre-set reasons so moderators don't get confused with times
- A /check command to see a players Punishment History and check with the ID
- Able to forgive (unban/unmute) a charge with the /check command
- Punishments go IP wide
- /checkip <ip|player> to check all accounts with one IP
- Also a unban command as an alias for /check forgive (only for bans)

---

## Default Config


<details>
<summary>config.json</summary>


```
{
  "punishments": {
    "toxicity": {
      "category": "toxicity",
      "displayReason": "Toxic behavior is not tolerated.",
      "type": "MUTE",
      "steps": [
        {
          "offense": 1,
          "duration": 86400000
        },
        {
          "offense": 2,
          "duration": 604800000
        },
        {
          "offense": 3,
          "duration": -1
        }
      ]
    },
    "hacking": {
      "category": "hacking",
      "displayReason": "You have been caught hacking.",
      "type": "BAN",
      "steps": [
        {
          "offense": 1,
          "duration": 7776000000
        },
        {
          "offense": 2,
          "duration": 10368000000
        },
        {
          "offense": 3,
          "duration": -1
        }
      ]
    },
    "spam": {
      "category": "spam",
      "displayReason": "Please do not spam.",
      "type": "KICK",
      "steps": [
        {
          "offense": 1,
          "duration": 0
        },
        {
          "offense": 2,
          "duration": 3600000
        },
        {
          "offense": 3,
          "duration": 86400000
        }
      ]
    }
  },
  "banMessage": "§cYou are banned!\\n\\n§7Reason: §f{reason}\\n§7ID: §f#{banId}\\n§7Expires: §f{expiry}",
  "kickMessage": "§cYou have been kicked!\\n\\n§7Reason: §f{reason}\\n§7ID: §f#{banId}",
  "muteMessage": "§cYou are muted!\\n\\n§7Reason: §f{reason}\\n§7ID: §f#{banId}\\n§7Expires: §f{expiry}"
}
```


</details>

You are able to set reasons and what to do after what times the player gets punished.



<details>
<summary>Types</summary>

MUTE

BAN

KICK

</details>


