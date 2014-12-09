(ns string.core
  (:require [clojure.string :as str]
            [goog.string :as gstr]))

(defn lower
  "Converts string to all lower-case."
  [s]
  (str/lower-case s))

(defn upper
  "Converts string to all upper-case."
  [s]
  (str/upper-case s))

(defn capitalize
  "Converts first letter of the string to uppercase."
  [s]
  (str/capitalize s))

(defn collapse-whitespace
  "Converts all adjacent whitespace characters
  to a single space."
  [s]
  (gstr/collapseWhitespace s))

(defn contains?
  "Determines whether a string contains a substring."
  [s subs]
  (gstr/contains s subs))

(defn startswith?
  "Check if the string starts with prefix."
  [s prefix]
  (gstr/startsWith s prefix))

(defn endswith?
  "Check if the string ends with suffix."
  [s prefix]
  (gstr/endsWith s prefix))

(defn camel-case
  "Converts a string from selector-case to camelCase."
  [s]
  (gstr/toCamelCase s))

(defn selector-case
  "Converts a string from camelCase to selector-case."
  [s]
  (gstr/toSelectorCase s))

(defn title-case
  "Converts a string into TitleCase."
  ([s]
   (gstr/toTitleCase s))
  ([s delimiters]
   (gstr/toTitleCase s delimiters)))

(defn escape-regexp
  "Escapes characters in the string that are not safe
  to use in a RegExp."
  [s]
  (gstr/regExpEscape s))

(defn trim
  "Removes whitespace or specified characters
  from both ends of string."
  ([s] (trim s " "))
  ([s chs]
   (let [rxstr (str "[" (escape-regexp chs) "]")
         rx    (js/RegExp. (str "^" rxstr "+|" rxstr "+$") "g")]
     (.replace s rx ""))))

(defn rtrim
  "Removes whitespace or specified characters
  from right side of string."
  ([s] (rtrim s " "))
  ([s chs]
   (let [rxstr (str "[" (escape-regexp chs) "]")
         rx    (js/RegExp. (str rxstr "+$"))]
     (.replace s rx ""))))

(defn ltrim
  "Removes whitespace or specified characters
  from left side of string."
  ([s] (ltrim s " "))
  ([s chs]
   (let [rxstr (str "[" (escape-regexp chs) "]")
         rx    (js/RegExp. (str "^" rxstr "+"))]
     (.replace s rx ""))))

(defn empty?
  "Checks if a string is empty or contains only whitespaces."
  [s]
  (gstr/isEmpty s))

(defn repeat
  "Repeats string n times."
  ([s] (repeat s 1))
  ([s n]
   (gstr/repeat s n)))

(defn strip-newlines
  "Takes a string and replaces newlines with a space.
  Multiple lines are replaced with a single space."
  [s]
  (gstr/stripNewlines s))

(defn split
  "Splits a string on a separator a limited
  number of times. The separator can be a string
  or RegExp instance."
  ([s] (split s #"\s" nil))
  ([s sep] (split s sep 0))
  ([s sep num]
   (if (regexp? sep)
     (str/split s sep num)
     (str/split s (re-pattern sep) num))))

(defn slice
  "Extracts a section of a string and returns a new string."
  ([s begin]
   (.slice s begin))
  ([s begin end]
   (.slice s begin end)))

(defn replace
  [s match replacement]
  (cond
   (string? match)
   (.replace s (js/RegExp. (escape-regexp match) "g") replacement)

   (regexp? match)
   (.replace s (js/RegExp. (.-source match) "g") replacement)

   :else
   (throw (str "Invalid match arg: " match))))

(defn replace-first
  [s match replacement]
  (cond
   (string? match)
   (.replace s (js/RegExp. (escape-regexp match)) replacement)

   (regexp? match)
   (.replace s (js/RegExp. (.-source match)) replacement)

   :else
   (throw (str "Invalid match arg: " match))))

(defn prune
  "Truncates a string to a certain length and adds '...'
  if necessary."
  ([s num] (prune s num "..."))
  ([s num subs]
   (if (< (count s) num)
     s
     (let [tmpl (fn [c] (if (not= (upper c) (lower c)) "A" " "))
           template (-> (slice s 0 (inc (count s)))
                        (replace #".(?=\W*\w*$)" tmpl))
           template (if (.match (slice template (- (count template) 2)) #"\w\w")
                      (replace-first template #"\s*\S+$" "")
                      (rtrim (slice template 0 (dec (count template)))))]
       (if (> (count (str template subs)) (count s))
         s
         (str (slice s 0 (count template)) subs))))))

(defn join
  "Joins strings together with given separator."
  ([coll]
     (apply str coll))
  ([separator coll]
     (apply str (interpose separator coll))))

(defn surround
  "Surround a string with another string."
  [s wrap]
  (join "" [wrap s wrap]))

(defn quote
  "Quotes a string."
  ([s] (surround s "\""))
  ([s qchar] (surround s qchar)))

(defn unquote
  "Unquote a string."
  ([s] (unquote s "\""))
  ([s qchar]
   (let [length (count s)
         fchar (aget s 0)
         lchar (aget s (dec length))]
     (if (and (= fchar qchar) (= lchar qchar))
       (slice s 1 (dec length))
       s))))

(defn dasherize
  "Converts a underscored or camelized string into an dasherized one."
  [s]
  ;; _s.trim(str).replace(/([A-Z])/g, '-$1').replace(/[-_\s]+/g, '-').toLowerCase();
  (-> s
      (trim)
      (replace #"([A-Z])" "-$1")
      (replace #"[-_\s]+" "-")
      (lower)))

(defn slugify
  "Transform text into a URL slug."
  [s]
  (let [from   "ąàáäâãåæăćčĉęèéëêĝĥìíïîĵłľńňòóöőôõðøśșšŝťțŭùúüűûñÿýçżźž"
        to     "aaaaaaaaaccceeeeeghiiiijllnnoooooooossssttuuuuuunyyczzz",
        regex  (js/RegExp. (str "[" (escape-regexp from) "]"))]
    (-> (lower s)
        (replace regex (fn [c]
                         (let [index (.indexOf from c)
                               res   (.charAt to index)]
                           (if (empty? res) "-" res))))
        (replace #"[^\w\s-]" "")
        (dasherize))))
