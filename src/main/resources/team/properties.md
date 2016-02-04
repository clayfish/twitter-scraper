# application.properties
This file is the only configuration file in this project. Values can use values of other properties defined in this file with simple syntax. Consider following example.

If `application.properties` contains these two lines.
```
...
output.folder=output-for-${target.step}
target.step=2
...
```
This will be resolved to following values,
``` {java}
String outputFolder = "output-for-2";
int targetStep = 2;
```
Order of these properties does not matter.

## Complete fields
It has following properties which can be changed/set to make the scraper behave accordingly.

Sr | Property | Type | Default | Step | Detail
--- | ------- | ---- | ------- | ------ | -----------
1 | target.username | String, required |  | 1, 2 | The twitter username to fetch the conversations for
2 | target.starting-tweet | long |  | 1 | Starting to fetch the tweet IDs starting from this ID
3 | target.last-tweet | long |  | 1 | Stop to fetch tweet IDs once this ID is fetched
 4 | target.continue | boolean | `false` | 1, 2 | Whether to start where it left last time (Currently being ignored)
 5 | target.step | int, required |  | 1, 2 | The step to run. Possible values are `1` or `2`.
 6 | mode | Mode | `DEV` | 1, 2 | Possible values are `DEV`, `TEST`, `PROD`. In `TEST` mode, no http request is made
 7 | output.file.extension | String | `".csv"` | 1, 2 | Please do not change it.
 8 | user-agent | String | "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36" | 1, 2 | Change at your own risk.
 9 | connection.timeout | long | 30000 | 1, 2 | Timeout for http requests
 10 | output.folder | String | "output/${target.username}" | 1, 2 | Folder to put the fetched data
 11 | state-file | String | "${output-folder}/saved-state.${output.file.extension}" | 2 | Files to persist the state of the scraper while fetching the conversations
 12 | base-url | String | "https://twitter.com/${target.username}/with_replies" | 1, 2 | Please do not change it.
 13 | concurrent-threads | long, required | 10 | 2 | Number of threads to run to fetch the conversations
 