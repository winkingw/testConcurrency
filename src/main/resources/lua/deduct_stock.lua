  local stock = tonumber(redis.call('get', KEYS[1]))
  if not stock then
    return -1
  end
  local num = tonumber(ARGV[1])
  if stock < num then
    return 0
  end
  redis.call('set', KEYS[1], stock - num)
  return 1