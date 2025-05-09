---
trigger: always_on
---

1. before you start working can you read the development_guidelines.md and the readme.md as some principles to how to approach development on this project.
2. after you finish each todo item in the todo_list.md run `bin/start_and_healthcheck.sh` and fix any linting or startup errors. if the application starts up and runs (you can do a health check against the status endpoint), then you can move on to the next todo item.
3. if you notice there is something that is missing and it seems like it is something that should be part of our general patterns/architectures please update (like updating the classpath when adding new db migrations)