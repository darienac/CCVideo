local HOSTNAME = "{HOSTNAME}"
local PORT = "{PORT}"
local PACKET_SIZE_MAX = {PACKET_SIZE_MAX}

local args = {...}
local data
local readPos = 1

local width
local height
local fps
local tape = peripheral.find("tape_drive")

function readByte()
    readPos = readPos + 1
    return string.byte(data, readPos-1)
end

function readShort()
    return readByte() * 256 + readByte()
end

--Code
local url = "http://" .. HOSTNAME .. ":" .. PORT
local page = http.get(url .. "/videoSize", {}, true)
local size = tonumber(page.readAll())
page.close()
local chunks = math.ceil(size / PACKET_SIZE_MAX)

data = ""
for i = 1, chunks do
    print("Loading chunk " .. i .. " of " .. chunks .. "..")
    local page = http.get(url .. "/videoChunk/" .. (i - 1), {}, true)
    data = data .. page.readAll()
    print(#data)
    page.close()
end

print("Playing..")

if (tape ~= nil) then
    tape.seek(-1000000000)
    tape.play()
end

width = readShort()/2
height = readShort()/3
fps = readByte()

while (readPos < #data) do
    local pSize = readByte()
    for i = 1, pSize do
        term.setPaletteColor(2^(i-1), colors.packRGB(readByte()/255, readByte()/255, readByte()/255))
        --readByte()
        --readByte()
        --readByte()
    end
    for y = 1, height, 1 do
        local text = ""
        local fg = ""
        local bg = ""
        for x = 1, width, 1 do
            text = text..string.char(readByte())
            fg = fg..string.format("%x",readByte())
            bg = bg..string.format("%x",readByte())
        end
        term.setCursorPos(1,y)
        term.blit(text,fg,bg)
    end
    sleep(1/fps)
end
sleep(2)
tape.stop()