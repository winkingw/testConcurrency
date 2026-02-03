local key = KEYS[1]
local decr = tonumber(ARGV[1])

local stock = tonumber(redis.call("GET",key))

if stock == nil then
    return -1
end
if stock < decr then
    return 0
end
redis.call("DECRBY", key, decr)
return 1