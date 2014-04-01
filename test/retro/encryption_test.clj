(ns retro.encryption-test
  (:require [clojure.test :refer :all]
            [retro.encryption :refer :all]))

(deftest rc4-test
  (let [expected-table [37 110 146 136 135 250 143 88 73 130 142 155 84 240 75 82 42 106 192 195 183 80 214 53 114 1 70 39 109 131 160 178 71 68 4 196 141 8 81 40 65 6 48 102 90 113 201 49 56 74 217 190 55 35 165 18 188 98 107 161 78 86 158 140 159 223 177 14 120 243 137 111 69 72 50 208 30 249 76 238 170 22 66 38 89 219 95 184 92 151 152 255 213 174 58 209 221 242 163 193 118 126 7 43 112 207 46 122 121 156 26 129 128 62 91 132 244 169 167 254 47 164 12 168 218 34 246 182 24 162 100 231 138 205 172 227 61 148 204 63 57 15 185 222 54 108 11 229 171 101 175 115 189 116 248 211 200 99 17 145 252 5 83 210 215 28 198 228 153 125 186 77 19 10 144 31 104 117 225 180 147 241 64 127 181 199 191 16 52 226 253 133 29 45 154 41 60 27 124 87 119 220 36 251 194 206 234 216 212 239 233 232 176 44 149 235 157 0 173 25 97 32 51 93 187 230 236 21 79 247 96 150 105 237 23 103 202 3 134 33 203 9 13 197 59 2 123 139 166 67 85 94 20 179 224 245]]
    (testing "public key decode"
      (is (= 535360
             (decode-key "55wfe030o2b17933arq9512j5u111105ckp230c81rp3m61ew9er3y0d523"))))

    (testing "table initialization"
      (is (= expected-table
             (initialize-table 535360))))

    (testing "encipher"
      (let [cipher (make-cipher encipher expected-table)]
        (is (= "491A6874B370A3"
               (cipher "testing")))
        (is (= "39D063F45DA486CC795F27DB3A"
               (cipher "testing twice")))))

    (testing "decipher"
      (let [cipher (make-cipher decipher expected-table)]
        (is (= "testing"
               (cipher "491A6874B370A3")))
        (is (= "testing twice"
               (cipher "39D063F45DA486CC795F27DB3A")))))))
