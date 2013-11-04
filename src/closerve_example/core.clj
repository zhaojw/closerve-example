(ns closerve-example.core
  (:require [ring.util.response :as response]
            [ring.middleware.params :as params]
            [ring.util.response :as response])
  (:use [closerve state wscmd server])
  (:gen-class))


(register-lift-snippet
 "TestSlowLoad"
 [node req page-id lift-instr]
 (Thread/sleep 5000)
 (-> node 
     (assoc :content ["Lazy Load finished!"])
     (assoc-in [:attrs :class] "alert alert-success")
     )
 )

(def chat-chans (atom #{}))
(def chat-msgs (atom []))

(register-cmd-proc-fn 
 "ChatInput"
 [context cmd]
 (let [chat-msg (cmd "chatmsg")]
   (if (string? chat-msg)     
     (do 
       (swap! chat-msgs #(apply vector (take 10 (conj %1 %2))) chat-msg)
       (send-cmd-to-page (:page-id context) 
                         {:act :reset 
                          :selector 
                          (str "#" (:form-id context))})
       )       
     ))
 )

(add-watch chat-msgs :watch-chat-msgs
           (fn [key aref old-val new-val]
             (prn "new chat message:" (last new-val))))

(register-comet-fn
 "chat"
 [ch req page-id uuid]
 (swap! chat-chans conj ch)
 (loop [msg (<! ch)]
   (when msg 
     (prn "Ok, got msg")
     (recur (<! ch))))
 (prn "chat channel is closed")
 (swap! chat-chans disj ch)
)


;;;"no .html here"
(reset! access-rules
        [         
         [#"^/templates-hidden.*$" (fn [req] false)]
         ])

(defn -main 
  [serverdir & {:strs [host port dbhost]
                :or {host "127.0.0.1" port "9090" dbhost "localhost"}}]

  (reset! root-dir serverdir)
  (start-server host (Integer. port)))
