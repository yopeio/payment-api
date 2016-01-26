#!/bin/sh

git filter-branch --env-filter '

an="$GIT_AUTHOR_NAME"
am="$GIT_AUTHOR_EMAIL"
cn="$GIT_COMMITTER_NAME"
cm="$GIT_COMMITTER_EMAIL"

if [ "$GIT_COMMITTER_EMAIL" = "enrico.mariotti@akqa.com" ]
then
    cn="Enrico Mariotti"
    cm="enr_74@yahoo.it"
fi
if [ "$GIT_AUTHOR_EMAIL" = "enrico.mariotti@akqa.com" ]
then
    an="Enrico Mariotti"
    am="enr_74@yahoo.it"
fi

export GIT_AUTHOR_NAME="$an"
export GIT_AUTHOR_EMAIL="$am"
export GIT_COMMITTER_NAME="$cn"
export GIT_COMMITTER_EMAIL="$cm"
'