package scripts

def action = { logger.info "hello" }

return createTimer(action, 1000)